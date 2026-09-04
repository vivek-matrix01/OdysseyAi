import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, MapPin, Calendar, Clock, ArrowRight, MapPinned } from 'lucide-react';
import api from '../api/axiosConfig';
import Button from '../components/ui/Button';
import BorderGlow from '../components/ui/BorderGlow';

interface Trip {
  id: number;
  title: string;
  description?: string;
  destination: string;
  startDate: string;
  endDate: string;
  status: string;
}

// Destination-aware travel cover images
const getTripCoverImage = (trip: Trip): string => {
  const text = `${trip.destination || ''} ${trip.title || ''}`.toLowerCase();
  const seed = Math.abs(trip.id || 0);

  // 1. Coastal / Beaches / Tropical Islands
  if (
    text.includes('goa') || 
    text.includes('beach') || 
    text.includes('bali') || 
    text.includes('phuket') || 
    text.includes('maldives') || 
    text.includes('andaman') || 
    text.includes('pondicherry') || 
    text.includes('island') || 
    text.includes('coast') || 
    text.includes('sea')
  ) {
    const beachImages = [
      'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80', // Goa palm beach
      'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80', // White sand beach
      'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=800&q=80', // Golden hour ocean
      'https://images.unsplash.com/photo-1544644181-1484b3fdfc62?auto=format&fit=crop&w=800&q=80'  // Blue lagoon
    ];
    return beachImages[seed % beachImages.length];
  }

  // 2. Mountains / Hill Stations / Valleys
  if (
    text.includes('manali') || 
    text.includes('shimla') || 
    text.includes('leh') || 
    text.includes('ladakh') || 
    text.includes('kashmir') || 
    text.includes('himalaya') || 
    text.includes('mountain') || 
    text.includes('hill') || 
    text.includes('mussoorie') || 
    text.includes('rishikesh') || 
    text.includes('alps') || 
    text.includes('snow')
  ) {
    const mountainImages = [
      'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80', // Dramatic peaks
      'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80', // Mountain lake valley
      'https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=800&q=80', // Alpine snow
      'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=800&q=80'  // High mountain pass
    ];
    return mountainImages[seed % mountainImages.length];
  }

  // 3. Heritage / Royal / Palaces / Monuments
  if (
    text.includes('jaipur') || 
    text.includes('udaipur') || 
    text.includes('rajasthan') || 
    text.includes('agra') || 
    text.includes('taj') || 
    text.includes('delhi') || 
    text.includes('varanasi') || 
    text.includes('hampi') || 
    text.includes('palace') || 
    text.includes('fort') || 
    text.includes('temple') || 
    text.includes('heritage')
  ) {
    const heritageImages = [
      'https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=800&q=80', // Jaipur Hawa Mahal
      'https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=800&q=80', // Taj Mahal
      'https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?auto=format&fit=crop&w=800&q=80', // Historic temple monument
      'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=800&q=80'  // Ancient Indian architecture
    ];
    return heritageImages[seed % heritageImages.length];
  }

  // 4. Nature / Backwaters / Waterfalls / Forests
  if (
    text.includes('kerala') || 
    text.includes('alleppey') || 
    text.includes('munnar') || 
    text.includes('coorg') || 
    text.includes('wayanad') || 
    text.includes('waterfall') || 
    text.includes('forest') || 
    text.includes('lake') || 
    text.includes('nature') || 
    text.includes('green')
  ) {
    const natureImages = [
      'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80', // Kerala houseboat
      'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=800&q=80', // Lush waterfall
      'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=800&q=80', // Misty pine forest
      'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80'  // Rolling foggy hills
    ];
    return natureImages[seed % natureImages.length];
  }

  // 5. Urban / Metropolitan / International Skylines
  if (
    text.includes('dubai') || 
    text.includes('paris') || 
    text.includes('london') || 
    text.includes('tokyo') || 
    text.includes('singapore') || 
    text.includes('new york') || 
    text.includes('mumbai') || 
    text.includes('bangalore') || 
    text.includes('city')
  ) {
    const cityImages = [
      'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80', // Dubai skyline
      'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80', // Paris Eiffel Tower
      'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80', // Tokyo city lights
      'https://images.unsplash.com/photo-1477959858617-67f30bc75b82?auto=format&fit=crop&w=800&q=80'  // Metropolitan skyline
    ];
    return cityImages[seed % cityImages.length];
  }

  // 6. Default Curated Scenic Travel Photography
  const defaultTravelImages = [
    'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80', // Travel wanderlust
    'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80', // Scenic road trip
    'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=800&q=80', // Mountain lake boat voyage
    'https://images.unsplash.com/photo-1503220317375-aaad61436b1b?auto=format&fit=crop&w=800&q=80', // Overlook viewpoint
    'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800&q=80'  // Airplane flight
  ];
  return defaultTravelImages[seed % defaultTravelImages.length];
};

const getDestinationFallbackImage = (id?: number): string => {
  const fallbacks = [
    'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=800&q=80',
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80'
  ];
  return fallbacks[Math.abs(id || 0) % fallbacks.length];
};

const Dashboard: React.FC = () => {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        const response = await api.get('/trips');
        setTrips(response.data.data?.content || []);
      } catch (error) {
        console.error('Failed to fetch trips', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTrips();
  }, []);

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3 text-secondary-500">
        <MapPinned className="h-8 w-8 animate-spin text-primary-600" />
        <p className="text-sm font-medium">Loading your adventures...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 pb-12">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-secondary-900 tracking-tight">Your Trips</h1>
          <p className="text-secondary-500 text-sm mt-1">Manage, explore, and view itineraries for all your upcoming adventures.</p>
        </div>
        <Link to="/ai-recommendations">
          <Button className="flex items-center gap-2 shadow-sm">
            <Plus className="h-4 w-4" /> Plan New Trip
          </Button>
        </Link>
      </div>

      {trips.length === 0 ? (
        <div className="bg-white border border-secondary-200 rounded-2xl p-6 sm:p-12 text-center shadow-xs">
          <div className="bg-primary-50 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-primary-100">
            <MapPin className="h-8 w-8 text-primary-600" />
          </div>
          <h3 className="text-lg font-bold text-secondary-900 mb-2">No trips planned yet</h3>
          <p className="text-secondary-500 mb-6 max-w-md mx-auto text-sm">
            Ready to explore? Use our AI travel planner to generate an amazing, customized itinerary for your next vacation.
          </p>
          <Link to="/ai-recommendations">
            <Button size="lg" className="shadow-sm">Start Planning</Button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 sm:gap-6">
          {trips.map((trip) => {
            const start = trip.startDate ? new Date(trip.startDate) : null;
            const end = trip.endDate ? new Date(trip.endDate) : null;
            let durationText = '';
            if (start && end) {
              const diffTime = Math.abs(end.getTime() - start.getTime());
              const diffDays = Math.max(1, Math.round(diffTime / (1000 * 60 * 60 * 24)));
              durationText = `${diffDays} ${diffDays === 1 ? 'day' : 'days'}`;
            }

            return (
              <Link key={trip.id} to={`/trips/${trip.id}`} className="group block h-full">
                <BorderGlow
                  edgeSensitivity={30}
                  glowColor="40 80 80"
                  backgroundColor="#ffffff"
                  borderRadius={16}
                  glowRadius={40}
                  glowIntensity={1}
                  coneSpread={25}
                  animated={false}
                  colors={['#c084fc', '#f472b6', '#38bdf8']}
                  className="h-full hover:shadow-xl transition-all duration-300"
                >
                  <div className="overflow-hidden h-full flex flex-col">
                    {/* Card Cover Image with Overlay */}
                    <div className="h-48 bg-secondary-200 relative overflow-hidden">
                      <img
                        src={getTripCoverImage(trip)}
                        alt={trip.title || trip.destination}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ease-out"
                        onError={(e) => {
                          e.currentTarget.onerror = null;
                          e.currentTarget.src = getDestinationFallbackImage(trip.id);
                        }}
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/30 to-transparent"></div>
                      
                      {/* Status Badge */}
                      <div className="absolute top-3 right-3">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold shadow-sm backdrop-blur-md ${
                          trip.status === 'COMPLETED'
                            ? 'bg-emerald-500/90 text-white'
                            : trip.status === 'ONGOING'
                            ? 'bg-blue-500/90 text-white'
                            : trip.status === 'CANCELLED'
                            ? 'bg-red-500/90 text-white'
                            : 'bg-white/95 text-secondary-900 border border-white/40'
                        }`}>
                          {trip.status || 'PLANNED'}
                        </span>
                      </div>

                      {/* Duration Badge if available */}
                      {durationText && (
                        <div className="absolute top-3 left-3">
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-black/50 text-white backdrop-blur-md border border-white/20 shadow-xs">
                            <Clock className="h-3 w-3" /> {durationText}
                          </span>
                        </div>
                      )}

                      {/* Title & Destination on Image */}
                      <div className="absolute bottom-3 left-4 right-4">
                        <h3 className="text-lg font-bold text-white truncate drop-shadow-sm group-hover:text-primary-200 transition-colors">
                          {trip.title}
                        </h3>
                        <p className="text-xs text-secondary-200 flex items-center gap-1 mt-0.5 font-medium truncate">
                          <MapPin className="h-3.5 w-3.5 text-primary-400 shrink-0" /> {trip.destination}
                        </p>
                      </div>
                    </div>

                    {/* Card Body */}
                    <div className="p-5 flex-grow flex flex-col justify-between gap-4">
                      <div className="space-y-2">
                        {trip.description && (
                          <p className="text-xs text-secondary-500 line-clamp-2 leading-relaxed">
                            {trip.description}
                          </p>
                        )}
                        <div className="flex items-center text-xs text-secondary-600 gap-2 font-medium">
                          <Calendar className="h-3.5 w-3.5 text-primary-500 shrink-0" />
                          <span>
                            {trip.startDate ? new Date(trip.startDate).toLocaleDateString() : 'TBD'} - {trip.endDate ? new Date(trip.endDate).toLocaleDateString() : 'TBD'}
                          </span>
                        </div>
                      </div>

                      <div className="pt-3 border-t border-secondary-100 flex items-center justify-between">
                        <span className="text-xs text-secondary-400 font-medium">Trip ID: #{trip.id}</span>
                        <span className="inline-flex items-center gap-1 text-xs font-bold text-primary-600 group-hover:text-primary-700 group-hover:translate-x-0.5 transition-all">
                          View Itinerary <ArrowRight className="h-3.5 w-3.5" />
                        </span>
                      </div>
                    </div>
                  </div>
                </BorderGlow>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
