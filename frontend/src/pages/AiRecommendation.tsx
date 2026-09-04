import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import BorderGlow from '../components/ui/BorderGlow';
import { Sparkles, Map, Calendar, Wallet, Check, Hotel, MapPin, Loader2, AlertTriangle, AlertCircle, Search, CheckSquare, Square } from 'lucide-react';
import { 
  TripRecommendationControllerService, 
  TripRecommendationConfirmationControllerService,
  AiControllerService
} from '../api/generated';
import type {
  TripRecommendationResponse,
  RecommendedPlaceResponse,
  RecommendedHotelResponse
} from '../api/generated';

const AiRecommendation: React.FC = () => {
  // Step 1: Input Form
  const [formData, setFormData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    budget: '',
    interests: '',
    travelers: 1,
    tripType: 'Vacation'
  });
  
  // State for flow
  const [step, setStep] = useState<1 | 2>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  
  // Step 2: Recommendations & Selection
  const [recommendation, setRecommendation] = useState<TripRecommendationResponse | null>(null);
  const [selectedPlaces, setSelectedPlaces] = useState<Set<string>>(new Set());
  const [selectedHotel, setSelectedHotel] = useState<string | null>(null);
  const [placeCategoryFilter, setPlaceCategoryFilter] = useState<string>('ALL');
  const [placeSearchQuery, setPlaceSearchQuery] = useState<string>('');
  const [currencyMode, setCurrencyMode] = useState<'INR' | 'LOCAL'>('INR');

  const isLocalCurrencyAvailable = Boolean(
    recommendation?.localCurrency &&
    recommendation.localCurrency.toUpperCase() !== 'INR' &&
    recommendation.exchangeRateFromInr &&
    recommendation.exchangeRateFromInr > 0
  );

  const localRate = isLocalCurrencyAvailable ? (recommendation?.exchangeRateFromInr || 1) : 1;
  const localSymbol = isLocalCurrencyAvailable ? (recommendation?.localCurrencySymbol || '') : '₹';
  const localCode = isLocalCurrencyAvailable ? (recommendation?.localCurrency || 'INR') : 'INR';

  const formatPrice = (inrAmount?: number | null) => {
    if (inrAmount == null) return '₹0';
    if (currencyMode === 'LOCAL' && isLocalCurrencyAvailable) {
      const converted = Math.round(inrAmount * localRate);
      return `${localSymbol}${converted.toLocaleString()} ${localCode}`;
    }
    return `₹${Math.round(inrAmount).toLocaleString()}`;
  };
  
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errorMessage) setErrorMessage(null);
  };

  // -------------------------------------------------------------
  // STEP 1: Fetch Initial Recommendations
  // -------------------------------------------------------------
  const handleFetchRecommendations = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const response = await TripRecommendationControllerService.recommend({
        tripType: formData.tripType,
        destination: formData.destination,
        budget: parseFloat(formData.budget),
        currency: 'INR',
        travelers: parseInt(formData.travelers.toString()),
        startDate: formData.startDate,
        endDate: formData.endDate,
        preferences: formData.interests,
        includeHotels: true
      });
      
      if (response.data) {
        setRecommendation(response.data);
        
        // Pre-select the first hotel as default
        if (response.data.hotels && response.data.hotels.length > 0) {
          setSelectedHotel(response.data.hotels[0].propertyToken || null);
        }

        // Pre-select top 3 recommended places so initial budget is never zero
        if (response.data.places && response.data.places.length > 0) {
          const initialSet = new Set<string>();
          response.data.places.slice(0, 4).forEach((p) => {
            if (p.placeId) initialSet.add(p.placeId);
          });
          setSelectedPlaces(initialSet);
        }
        
        // Advance to selection step
        setStep(2);
        toast.success('Recommendations ready!');
      }
    } catch (err: any) {
      const msg = err.body?.message || err.response?.data?.message || err.message || 'Sorry, we do not provide services at this location.';
      setErrorMessage(msg);
      toast.error(msg, { duration: 6000 });
    } finally {
      setIsLoading(false);
    }
  };

  // -------------------------------------------------------------
  // STEP 2: Selection Logic & Budget Calculation
  // -------------------------------------------------------------
  const getTripNights = () => {
    if (!formData.startDate || !formData.endDate) return 2;
    const start = new Date(formData.startDate);
    const end = new Date(formData.endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return Math.max(1, diffDays);
  };

  const getHotelCost = (hotel: RecommendedHotelResponse | undefined) => {
    if (!hotel) return 0;
    if (hotel.totalCost && hotel.totalCost > 0) return hotel.totalCost;
    if (hotel.estimatedNightlyCost && hotel.estimatedNightlyCost > 0) {
      return hotel.estimatedNightlyCost * getTripNights();
    }
    return 2500 * getTripNights();
  };

  const getPlaceCost = (place: RecommendedPlaceResponse | undefined) => {
    if (!place) return 0;
    if (place.estimatedCost != null && place.estimatedCost > 0) return place.estimatedCost;
    return 200;
  };

  const togglePlace = (placeId: string) => {
    const newSelection = new Set(selectedPlaces);
    if (newSelection.has(placeId)) {
      newSelection.delete(placeId);
    } else {
      newSelection.add(placeId);
    }
    setSelectedPlaces(newSelection);
  };

  const getSelectedHotelRequest = () => {
    if (!selectedHotel || !recommendation?.hotels) {
      return undefined;
    }

    const hotel = recommendation.hotels.find((item) => item.propertyToken === selectedHotel);

    if (!hotel) {
      return undefined;
    }

    const resolvedNightly = hotel.estimatedNightlyCost || Math.round(getHotelCost(hotel) / getTripNights());
    const resolvedTotal = getHotelCost(hotel);

    return {
      propertyToken: hotel.propertyToken,
      name: hotel.name,
      address: hotel.address,
      rating: hotel.rating,
      reviewCount: hotel.reviewCount,
      hotelClass: hotel.hotelClass,
      nightlyPrice: resolvedNightly,
      totalPrice: resolvedTotal,
      currency: hotel.currency || 'INR',
      priceDisplay: hotel.priceDisplay,
      amenities: hotel.amenities,
      imageUrls: hotel.imageUrls,
    };
  };

  // Calculate dynamic budget based on selections
  const calculateDynamicBudget = () => {
    let total = 0;
    
    // Add selected hotel cost
    if (selectedHotel && recommendation?.hotels) {
      const hotel = recommendation.hotels.find(h => h.propertyToken === selectedHotel);
      total += getHotelCost(hotel);
    }
    
    // Add selected places cost
    if (recommendation?.places) {
      recommendation.places.forEach(place => {
        if (place.placeId && selectedPlaces.has(place.placeId)) {
          total += getPlaceCost(place);
        }
      });
    }
    
    return Math.round(total);
  };

  // Curated backup images by place category and name
  const getPlaceBackupImage = (place: RecommendedPlaceResponse) => {
    const combined = `${place.name || ''} ${place.category || ''} ${place.address || ''}`.toLowerCase();
    let hash = 0;
    for (let i = 0; i < (place.name || '').length; i++) {
      hash = (hash << 5) - hash + place.name!.charCodeAt(i);
    }
    const safeHash = Math.abs(hash);

    if (combined.includes('beach') || combined.includes('coast') || combined.includes('sea') || combined.includes('island')) {
      const beaches = [
        'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=600&q=80'
      ];
      return beaches[safeHash % beaches.length];
    }
    if (combined.includes('temple') || combined.includes('church') || combined.includes('fort') || combined.includes('palace') || combined.includes('monument')) {
      const heritage = [
        'https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=600&q=80'
      ];
      return heritage[safeHash % heritage.length];
    }
    if (combined.includes('falls') || combined.includes('waterfall') || combined.includes('wildlife') || combined.includes('sanctuary') || combined.includes('park') || combined.includes('forest')) {
      const nature = [
        'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=600&q=80'
      ];
      return nature[safeHash % nature.length];
    }
    if (combined.includes('restaurant') || combined.includes('cafe') || combined.includes('food') || combined.includes('dining')) {
      const food = [
        'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80',
        'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=600&q=80'
      ];
      return food[safeHash % food.length];
    }

    const general = [
      'https://images.unsplash.com/photo-1517869662768-5c25ce264969?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=600&q=80',
      'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=600&q=80'
    ];
    return general[safeHash % general.length];
  };

  const getHotelBackupImage = (hotel: RecommendedHotelResponse) => {
    const name = (hotel.name || '').toLowerCase();
    let hash = 0;
    for (let i = 0; i < (hotel.name || '').length; i++) {
      hash = (hash << 5) - hash + hotel.name!.charCodeAt(i);
    }
    const safeHash = Math.abs(hash);

    if (name.includes('resort') || name.includes('beach') || name.includes('villa') || name.includes('spa')) {
      const resorts = [
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80'
      ];
      return resorts[safeHash % resorts.length];
    }

    const rooms = [
      'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80'
    ];
    return rooms[safeHash % rooms.length];
  };

  // Filtered places across the comprehensive catalog
  const filteredPlaces = (recommendation?.places || []).filter((p) => {
    const q = placeSearchQuery.trim().toLowerCase();
    const matchesQuery = !q || (p.name || '').toLowerCase().includes(q) || (p.address || '').toLowerCase().includes(q);
    if (!matchesQuery) return false;

    if (placeCategoryFilter === 'ALL') return true;
    const cat = (p.category || '').toLowerCase();
    if (placeCategoryFilter === 'ATTRACTION') {
      return !cat.includes('restaurant') && !cat.includes('food') && !cat.includes('cafe');
    }
    if (placeCategoryFilter === 'CULTURE') {
      return cat.includes('museum') || cat.includes('temple') || cat.includes('church') || cat.includes('historic') || cat.includes('monument');
    }
    if (placeCategoryFilter === 'NATURE') {
      return cat.includes('park') || cat.includes('beach') || cat.includes('garden') || cat.includes('view') || cat.includes('lake') || cat.includes('fall');
    }
    if (placeCategoryFilter === 'FOOD') {
      return cat.includes('restaurant') || cat.includes('food') || cat.includes('cafe') || cat.includes('bar');
    }
    return true;
  });

  const selectAllFilteredPlaces = () => {
    const next = new Set(selectedPlaces);
    filteredPlaces.forEach(p => {
      if (p.placeId) next.add(p.placeId);
    });
    setSelectedPlaces(next);
  };

  const clearFilteredPlaces = () => {
    const next = new Set(selectedPlaces);
    filteredPlaces.forEach(p => {
      if (p.placeId) next.delete(p.placeId);
    });
    setSelectedPlaces(next);
  };

  const getPredictedProblems = (): string[] => {
    if (recommendation?.potentialProblems && recommendation.potentialProblems.length > 0) {
      return recommendation.potentialProblems;
    }
    return [
      `Weather & Sudden Showers: Regional precipitation or peak afternoon heat can delay outdoor sightseeing. Keep an umbrella handy and plan outdoor stops before 11:00 AM or after 4:00 PM.`,
      `Peak Season Traffic & Distances: Commute times between tourist clusters can double during evening rush hours in ${formData.destination || 'the area'}. We recommend clustering geographically adjacent places together.`,
      `Monument Weekly Off-Days: Various state monuments and museums observe weekly closures (typically on Mondays). Confirm site operational hours before visiting.`,
      `Local Transport Surge & Entry Delays: Peak hours may incur taxi surge fares and ticket counters queue delays. Booking tickets online in advance is advised.`
    ];
  };

  // -------------------------------------------------------------
  // ACTION: Manual Plan (Builds itinerary from selected items)
  // -------------------------------------------------------------
  const handleManualPlan = async () => {
    if (selectedPlaces.size === 0) {
      toast.error('Please select at least one attraction!');
      return;
    }
    setIsLoading(true);
    try {
      // 1. Confirm and create the trip with selections
      const confirmResponse = await TripRecommendationConfirmationControllerService.confirm({
        title: `Manual Trip to ${formData.destination}`,
        description: 'Custom curated trip from AI recommendations',
        destination: formData.destination,
        startDate: formData.startDate,
        endDate: formData.endDate,
        selectedHotelPropertyToken: selectedHotel || undefined,
        selectedPlaceIds: Array.from(selectedPlaces)
      });
      
      const tripId = confirmResponse.data?.trip?.id;
      
      if (!tripId) {
        throw new Error("Failed to get Trip ID from backend response");
      }
      
      // 2. Build the itinerary from ONLY the selected places
      const hotelData = getSelectedHotelRequest();
      
      await AiControllerService.generateSelectedItinerary({
        tripId: tripId,
        hotel: hotelData,
        selectedPlaceIds: Array.from(selectedPlaces),
        currency: 'INR'
      });
      
      toast.success('Your custom trip has been created!');
      navigate(`/trips/${tripId}`);
    } catch (err: any) {
      toast.error(err.body?.message || err.message || 'Failed to create manual plan.');
    } finally {
      setIsLoading(false);
    }
  };

  // -------------------------------------------------------------
  // ACTION: Auto / AI Plan (Builds full itinerary ignoring selections)
  // -------------------------------------------------------------
  const handleAutoPlan = async () => {
    setIsLoading(true);
    try {
      // 1. Confirm and create the trip (no selections passed)
      const confirmResponse = await TripRecommendationConfirmationControllerService.confirm({
        title: `AI Trip to ${formData.destination}`,
        description: recommendation?.recommendationReason || 'Full AI Generated Trip',
        destination: formData.destination,
        startDate: formData.startDate,
        endDate: formData.endDate
      });
      
      const tripId = confirmResponse.data?.trip?.id;
      
      if (!tripId) {
        throw new Error("Failed to get Trip ID from backend response");
      }
      
      // 2. Ask AI to fully generate the itinerary (it will pick places)
      const start = new Date(formData.startDate);
      const end = new Date(formData.endDate);
      const days = Math.max(1, Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)));

      await AiControllerService.generateItinerary(tripId, {
        preferences: formData.interests.trim() || 'General tourism and sightseeing',
        numberOfDays: days,
        currency: 'INR'
      });
      
      toast.success('AI has planned your entire trip!');
      navigate(`/trips/${tripId}`);
    } catch (err: any) {
      toast.error(err.body?.message || err.message || 'Failed to generate AI plan.');
    } finally {
      setIsLoading(false);
    }
  };

  // =============================================================
  // RENDER: STEP 1 (Input Form)
  // =============================================================
  if (step === 1) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center p-3 bg-primary-100 rounded-full mb-4">
            <Sparkles className="h-8 w-8 text-primary-600" />
          </div>
          <h1 className="text-3xl font-bold text-secondary-900 mb-2">AI Trip Planner</h1>
          <p className="text-secondary-600 max-w-xl mx-auto">
            Tell us about your dream vacation, and our AI will craft the perfect personalized itinerary just for you.
          </p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-secondary-200 p-8">
          {errorMessage && (
            <div className="mb-6 bg-red-50 border border-red-200 text-red-800 rounded-xl p-4 flex items-start gap-3 text-sm shadow-xs">
              <AlertCircle className="h-5 w-5 text-red-600 shrink-0 mt-0.5" />
              <div>
                <h4 className="font-bold text-red-900">Destination Service Notice</h4>
                <p className="mt-0.5 text-red-700">{errorMessage}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleFetchRecommendations} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  <Map className="h-4 w-4 text-secondary-500" /> Where to?
                </label>
                <Input
                  name="destination"
                  required
                  value={formData.destination}
                  onChange={handleChange}
                  placeholder="e.g. Paris, Tokyo, Bali"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-secondary-500" /> Start Date
                </label>
                <Input
                  type="date"
                  name="startDate"
                  required
                  value={formData.startDate}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-secondary-500" /> End Date
                </label>
                <Input
                  type="date"
                  name="endDate"
                  required
                  value={formData.endDate}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  <Wallet className="h-4 w-4 text-secondary-500" /> Budget (INR)
                </label>
                <Input
                  type="number"
                  name="budget"
                  min="0"
                  step="100"
                  required
                  value={formData.budget}
                  onChange={handleChange}
                  placeholder="e.g. 50000"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  Trip Type
                </label>
                <Input
                  name="tripType"
                  required
                  value={formData.tripType}
                  onChange={handleChange}
                  placeholder="e.g. Honeymoon, Adventure, Solo"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1 flex items-center gap-2">
                  Travelers
                </label>
                <Input
                  type="number"
                  name="travelers"
                  min="1"
                  required
                  value={formData.travelers}
                  onChange={handleChange}
                />
              </div>

              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-secondary-700 mb-1">
                  Interests & Preferences (Optional)
                </label>
                <textarea
                  name="interests"
                  rows={3}
                  value={formData.interests}
                  onChange={handleChange}
                  placeholder="e.g. Historical sites, food markets, hiking..."
                  className="appearance-none block w-full px-3 py-2 border border-secondary-300 rounded-md shadow-sm placeholder-secondary-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
            </div>

            <div className="pt-4 border-t border-secondary-100">
              <Button type="submit" size="lg" fullWidth disabled={isLoading} className="text-base font-semibold">
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="h-5 w-5 animate-spin" /> Fetching Options...
                  </span>
                ) : (
                  'Discover Options'
                )}
              </Button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  // =============================================================
  // RENDER: STEP 2 (Cards & Manual/Auto Selection)
  // =============================================================
  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-20">
      
      {/* Header & Sticky Budget Bar */}
      <div className="sticky top-0 z-20 bg-white/95 backdrop-blur-md border-b border-secondary-200/90 py-3 sm:py-4 mb-6 sm:mb-8 shadow-xs">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 sm:gap-4">
          <div>
            <h1 className="text-xl sm:text-2xl font-extrabold text-secondary-900 tracking-tight">Choose Your Path</h1>
            <p className="text-secondary-500 text-xs sm:text-sm">Select places manually or let AI do all the work.</p>
          </div>
          
          <div className="flex flex-wrap sm:flex-nowrap items-center justify-between w-full md:w-auto gap-3 sm:gap-4 pt-2 md:pt-0 border-t md:border-t-0 border-secondary-100">
            <div className="flex items-center gap-3">
              {isLocalCurrencyAvailable && (
                <div className="inline-flex items-center bg-secondary-100 p-0.5 rounded-lg border border-secondary-200 text-xs">
                  <button
                    type="button"
                    onClick={() => setCurrencyMode('INR')}
                    className={`px-2 py-1 font-bold rounded-md transition-all ${
                      currencyMode === 'INR'
                        ? 'bg-white text-secondary-900 shadow-xs'
                        : 'text-secondary-500 hover:text-secondary-900'
                    }`}
                  >
                    ₹ INR
                  </button>
                  <button
                    type="button"
                    onClick={() => setCurrencyMode('LOCAL')}
                    className={`px-2 py-1 font-bold rounded-md transition-all flex items-center gap-1 ${
                      currencyMode === 'LOCAL'
                        ? 'bg-primary-600 text-white shadow-xs'
                        : 'text-secondary-500 hover:text-secondary-900'
                    }`}
                  >
                    <span>{localSymbol} {localCode}</span>
                  </button>
                </div>
              )}

              <div className="text-left md:text-right">
                <p className="text-[10px] sm:text-xs text-secondary-500 font-semibold uppercase tracking-wider">Final Total Amount</p>
                <p className="text-lg sm:text-2xl font-black text-primary-600">{formatPrice(calculateDynamicBudget())}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2 flex-1 sm:flex-initial justify-end">
              <Button 
                variant="outline" 
                size="sm" 
                onClick={handleManualPlan} 
                disabled={isLoading || selectedPlaces.size === 0}
                className="text-xs font-semibold px-2.5 sm:px-3 py-2 shrink-0"
              >
                {isLoading ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : 'Manual Plan'}
              </Button>
              <Button 
                size="sm" 
                onClick={handleAutoPlan} 
                disabled={isLoading} 
                className="bg-gradient-to-r from-primary-600 to-primary-500 text-xs font-bold px-3 py-2 shadow-xs shrink-0"
              >
                <Sparkles className="h-3.5 w-3.5 mr-1" />
                {isLoading ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : 'Auto AI Plan'}
              </Button>
            </div>
          </div>
        </div>
      </div>

      {/* Planning Mode Explainer & AI Risk Predictor */}
      <div className="space-y-4">
        {/* Mode comparison banner */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-white rounded-2xl border border-secondary-200 p-4 shadow-sm flex items-start gap-3.5">
            <div className="p-2.5 rounded-xl bg-secondary-100 text-secondary-700 shrink-0">
              <MapPin className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-bold text-secondary-900">Manual Travel Planning</h3>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-secondary-100 text-secondary-700">
                  {selectedPlaces.size} Places Selected
                </span>
              </div>
              <p className="text-xs text-secondary-600 mt-1">
                Browse every attraction and hotel found in {formData.destination}. Pick your favorites and click <strong>Create Manual Plan</strong>.
              </p>
            </div>
          </div>

          <div className="bg-gradient-to-r from-primary-50 to-indigo-50/60 rounded-2xl border border-primary-200/70 p-4 shadow-sm flex items-start gap-3.5">
            <div className="p-2.5 rounded-xl bg-primary-100 text-primary-600 shrink-0">
              <Sparkles className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-bold text-primary-950">AI Travel Planner</h3>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-primary-200 text-primary-800">
                  Smart Optimization
                </span>
              </div>
              <p className="text-xs text-primary-900/80 mt-1">
                Let AI curate the best overall plan matching your preferences, dates, and budget (₹{Number(formData.budget).toLocaleString()}) with automated schedule.
              </p>
            </div>
          </div>
        </div>

        {/* AI Travel Risk & Potential Problem Predictor */}
        <div className="bg-gradient-to-br from-amber-50/90 via-orange-50/40 to-amber-50/90 border-2 border-amber-200/90 rounded-2xl p-5 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-amber-200/60">
            <div className="flex items-center gap-2.5">
              <div className="p-2 bg-amber-100 text-amber-800 rounded-xl shrink-0">
                <AlertTriangle className="h-5 w-5" />
              </div>
              <div>
                <h3 className="text-base font-extrabold text-amber-950 flex items-center gap-2">
                  AI Trip Risk & Problem Predictor
                  <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-200 text-amber-900">
                    Proactive Travel Advice
                  </span>
                </h3>
                <p className="text-xs text-amber-900/80 mt-0.5">
                  Factors that could cause issues during your visit to {formData.destination} and how to avoid them.
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-4">
            {getPredictedProblems().map((problem, idx) => (
              <div key={idx} className="flex items-start gap-3 bg-white/95 rounded-xl p-3.5 border border-amber-100/90 shadow-2xs">
                <span className="w-5 h-5 rounded-full bg-amber-100 text-amber-800 font-bold flex items-center justify-center shrink-0 text-xs mt-0.5">
                  {idx + 1}
                </span>
                <p className="text-xs text-secondary-800 leading-relaxed font-medium">
                  {problem}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Hotels Section */}
      {recommendation?.hotels && recommendation.hotels.length > 0 && (
        <section>
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
            <div>
              <h2 className="text-xl font-bold text-secondary-900 flex items-center gap-2">
                <Hotel className="h-5 w-5 text-primary-500" /> All Available Stays & Accommodations
              </h2>
              <p className="text-xs text-secondary-500 mt-0.5">
                Showing all {recommendation.hotels.length} verified hotels in {formData.destination}. Click any stay to choose it.
              </p>
            </div>
            {selectedHotel && (
              <span className="text-xs font-semibold px-3 py-1 bg-primary-50 text-primary-700 border border-primary-200 rounded-full">
                Hotel selected for your plan
              </span>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {recommendation.hotels.map((hotel: RecommendedHotelResponse) => (
              <BorderGlow
                key={hotel.propertyToken}
                edgeSensitivity={30}
                glowColor="40 80 80"
                backgroundColor="#ffffff"
                borderRadius={16}
                glowRadius={40}
                glowIntensity={1}
                coneSpread={25}
                animated={false}
                colors={['#c084fc', '#f472b6', '#38bdf8']}
                className={`cursor-pointer transition-all ${
                  selectedHotel === hotel.propertyToken 
                    ? 'ring-4 ring-primary-300 shadow-md' 
                    : 'shadow-xs hover:shadow-md'
                }`}
              >
                <div 
                  onClick={() => setSelectedHotel(hotel.propertyToken || null)}
                  className="relative overflow-hidden flex flex-col h-full"
                >
                  {selectedHotel === hotel.propertyToken && (
                    <div className="absolute top-3 right-3 bg-primary-500 text-white p-1 rounded-full z-10 shadow-xs">
                      <Check className="h-4 w-4" />
                    </div>
                  )}
                  
                  <div className="h-40 bg-secondary-200 relative overflow-hidden">
                    <img 
                      src={hotel.imageUrls && hotel.imageUrls.length > 0 ? hotel.imageUrls[0] : getHotelBackupImage(hotel)} 
                      alt={hotel.name} 
                      className="w-full h-full object-cover" 
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = getHotelBackupImage(hotel);
                      }}
                    />
                    <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 p-4 pt-10">
                      <h3 className="text-white font-bold truncate">{hotel.name}</h3>
                    </div>
                  </div>
                  
                  <div className="p-4 bg-white flex-grow flex flex-col justify-between">
                    <div>
                      <div className="flex justify-between items-center mb-2">
                        <div>
                          <span className="text-sm font-bold text-secondary-900">{formatPrice(getHotelCost(hotel))} <span className="text-xs font-normal text-secondary-500">total</span></span>
                          {hotel.estimatedNightlyCost ? (
                            <p className="text-[11px] text-secondary-500">{formatPrice(hotel.estimatedNightlyCost)} / night</p>
                          ) : null}
                        </div>
                        <span className="text-xs font-medium px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full flex items-center">
                          ★ {hotel.rating}
                        </span>
                      </div>
                      <p className="text-xs text-secondary-500 line-clamp-2">{hotel.reason}</p>
                    </div>
                  </div>
                </div>
              </BorderGlow>
            ))}
          </div>
        </section>
      )}

      {/* Road Travel Distance & Vehicle Fuel Cost Estimator (Calculated for Selected Places) */}
      {/* Places Section */}
      {recommendation?.places && recommendation.places.length > 0 && (
        <section className="pt-4">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
            <div>
              <h2 className="text-xl font-bold text-secondary-900 flex items-center gap-2">
                <MapPin className="h-5 w-5 text-primary-500" /> All Tourist Places & Attractions ({recommendation.places.length})
              </h2>
              <p className="text-xs text-secondary-500 mt-0.5">
                Every attraction and experience found. Pick the places you want for manual planning or let AI choose.
              </p>
            </div>

            {/* Selection Quick Actions */}
            <div className="flex items-center gap-2">
              <button
                onClick={selectAllFilteredPlaces}
                className="text-xs font-semibold px-2.5 py-1.5 rounded-lg bg-secondary-100 text-secondary-700 hover:bg-secondary-200 transition-colors flex items-center gap-1.5"
              >
                <CheckSquare className="h-3.5 w-3.5 text-primary-600" /> Select All ({filteredPlaces.length})
              </button>
              <button
                onClick={clearFilteredPlaces}
                className="text-xs font-semibold px-2.5 py-1.5 rounded-lg bg-secondary-100 text-secondary-700 hover:bg-secondary-200 transition-colors flex items-center gap-1.5"
              >
                <Square className="h-3.5 w-3.5" /> Clear
              </button>
            </div>
          </div>

          {/* Search and Category Filter Bar */}
          <div className="flex flex-col sm:flex-row gap-3 items-center justify-between bg-secondary-50 p-3 rounded-xl border border-secondary-200 mb-6">
            <div className="relative w-full sm:w-72">
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-secondary-400" />
              <input
                type="text"
                placeholder="Search places by name or address..."
                value={placeSearchQuery}
                onChange={(e) => setPlaceSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-xs bg-white border border-secondary-300 rounded-lg focus:ring-primary-500 focus:border-primary-500"
              />
            </div>

            {/* Filter Pills */}
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full sm:w-auto pb-1 sm:pb-0">
              {[
                { id: 'ALL', label: `All (${recommendation.places.length})` },
                { id: 'ATTRACTION', label: 'Attractions' },
                { id: 'CULTURE', label: 'Culture & Heritage' },
                { id: 'NATURE', label: 'Nature & Parks' },
                { id: 'FOOD', label: 'Food & Dining' }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setPlaceCategoryFilter(tab.id)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-colors shrink-0 ${
                    placeCategoryFilter === tab.id
                      ? 'bg-primary-600 text-white shadow-xs'
                      : 'bg-white text-secondary-600 border border-secondary-200 hover:bg-secondary-100'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          {filteredPlaces.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-xl border border-dashed border-secondary-300">
              <p className="text-sm font-medium text-secondary-600">No places match your search query or filter.</p>
              <button
                onClick={() => { setPlaceCategoryFilter('ALL'); setPlaceSearchQuery(''); }}
                className="mt-2 text-xs font-semibold text-primary-600 hover:underline"
              >
                Reset Filters
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {filteredPlaces.map((place: RecommendedPlaceResponse) => {
                const isSelected = place.placeId ? selectedPlaces.has(place.placeId) : false;
                
                return (
                  <BorderGlow
                    key={place.placeId}
                    edgeSensitivity={30}
                    glowColor="40 80 80"
                    backgroundColor="#ffffff"
                    borderRadius={14}
                    glowRadius={40}
                    glowIntensity={1}
                    coneSpread={25}
                    animated={false}
                    colors={['#c084fc', '#f472b6', '#38bdf8']}
                    className={`cursor-pointer transition-all ${
                      isSelected 
                        ? 'ring-4 ring-primary-300 shadow-md' 
                        : 'shadow-xs hover:shadow-md'
                    }`}
                  >
                    <div 
                      onClick={() => place.placeId && togglePlace(place.placeId)}
                      className="overflow-hidden flex flex-col h-full"
                    >
                      <div className="h-32 relative overflow-hidden">
                        <img 
                          src={place.imageUrls && place.imageUrls.length > 0 ? place.imageUrls[0] : getPlaceBackupImage(place)}
                          alt={place.name} 
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.currentTarget.onerror = null;
                            e.currentTarget.src = getPlaceBackupImage(place);
                          }}
                        />
                        <div className="absolute top-2 right-2">
                          <div className={`p-1.5 rounded-full ${isSelected ? 'bg-primary-500 text-white' : 'bg-white/80 text-secondary-500 backdrop-blur-sm'}`}>
                            {isSelected ? <Check className="h-4 w-4" /> : <MapPin className="h-4 w-4" />}
                          </div>
                        </div>
                      </div>
                      
                      <div className="p-4 bg-white flex-grow flex flex-col justify-between">
                        <div>
                          <div className="flex justify-between items-start mb-1">
                            <h3 className="font-bold text-secondary-900 line-clamp-1">{place.name}</h3>
                            <span className="text-sm font-bold text-secondary-900 ml-2">{formatPrice(getPlaceCost(place))}</span>
                          </div>
                          <p className="text-xs text-secondary-500 mb-2">{place.category}</p>
                          <p className="text-xs text-secondary-600 line-clamp-2">{place.reason}</p>
                        </div>
                      </div>
                    </div>
                  </BorderGlow>
                );
              })}
            </div>
          )}
        </section>
      )}
    </div>
  );
};

export default AiRecommendation;
