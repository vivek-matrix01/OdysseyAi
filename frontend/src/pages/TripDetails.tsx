import React, { useEffect, useState, useMemo } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { 
  MapPin, 
  Calendar, 
  Clock, 
  ArrowLeft, 
  Loader2, 
  Save, 
  X, 
  Trash2, 
  Sun, 
  CloudRain, 
  Cloud, 
  Car, 
  Hotel,
  Users,
  UserPlus,
  Eye,
  Pencil,
  Crown,
  Mail
} from 'lucide-react';
import api from '../api/axiosConfig';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import ConfirmModal from '../components/ui/ConfirmModal';
import toast from 'react-hot-toast';
import { ItineraryControllerService, TripControllerService, WeatherControllerService } from '../api/generated';
import { useAuth } from '../context/AuthContext';

// Dynamic destination cover image resolver
const getTripCoverImage = (trip: any): string => {
  if (!trip) return 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1200&q=80';
  const text = `${trip.destination || ''} ${trip.title || ''}`.toLowerCase();
  const seed = Math.abs(trip.id || 0);

  if (text.includes('goa') || text.includes('beach') || text.includes('bali') || text.includes('phuket') || text.includes('maldives') || text.includes('andaman') || text.includes('pondicherry') || text.includes('island') || text.includes('coast')) {
    const beaches = [
      'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=1200&q=80'
    ];
    return beaches[seed % beaches.length];
  }
  if (text.includes('manali') || text.includes('shimla') || text.includes('leh') || text.includes('ladakh') || text.includes('kashmir') || text.includes('himalaya') || text.includes('mountain') || text.includes('hill') || text.includes('mussoorie') || text.includes('rishikesh')) {
    const mountains = [
      'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=1200&q=80'
    ];
    return mountains[seed % mountains.length];
  }
  if (text.includes('jaipur') || text.includes('udaipur') || text.includes('rajasthan') || text.includes('agra') || text.includes('taj') || text.includes('delhi') || text.includes('varanasi') || text.includes('hampi') || text.includes('palace') || text.includes('fort')) {
    const heritage = [
      'https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1200&q=80'
    ];
    return heritage[seed % heritage.length];
  }
  if (text.includes('kerala') || text.includes('alleppey') || text.includes('munnar') || text.includes('coorg') || text.includes('wayanad') || text.includes('waterfall') || text.includes('forest')) {
    const nature = [
      'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80'
    ];
    return nature[seed % nature.length];
  }
  if (text.includes('dubai') || text.includes('paris') || text.includes('tokyo') || text.includes('singapore') || text.includes('london') || text.includes('new york') || text.includes('city')) {
    const city = [
      'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1200&q=80',
      'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1200&q=80'
    ];
    return city[seed % city.length];
  }
  return 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80';
};

const TripDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user: authUser } = useAuth();

  const [trip, setTrip] = useState<any>(null);
  const [itinerary, setItinerary] = useState<any>(null);
  const [weatherMap, setWeatherMap] = useState<{ [date: string]: any }>({});
  const [isLoading, setIsLoading] = useState(true);
  const [currencyMode, setCurrencyMode] = useState<'INR' | 'LOCAL'>('INR');

  const isLocalCurrencyAvailable = Boolean(
    itinerary?.localCurrency &&
    itinerary.localCurrency.toUpperCase() !== 'INR' &&
    itinerary.exchangeRateFromInr &&
    itinerary.exchangeRateFromInr > 0
  );

  const localRate = isLocalCurrencyAvailable ? (itinerary?.exchangeRateFromInr || 1) : 1;
  const localSymbol = isLocalCurrencyAvailable ? (itinerary?.localCurrencySymbol || '') : '₹';
  const localCode = isLocalCurrencyAvailable ? (itinerary?.localCurrency || 'INR') : 'INR';

  const formatPrice = (inrAmount?: number | null) => {
    if (inrAmount == null) return '₹0';
    if (currencyMode === 'LOCAL' && isLocalCurrencyAvailable) {
      const converted = Math.round(inrAmount * localRate);
      return `${localSymbol}${converted.toLocaleString()} ${localCode}`;
    }
    return `₹${Math.round(inrAmount).toLocaleString()}`;
  };

  // Trip Members state
  const [members, setMembers] = useState<any[]>([]);
  const [isMembersLoading, setIsMembersLoading] = useState<boolean>(false);
  const [inviteEmail, setInviteEmail] = useState<string>('');
  const [inviteRole, setInviteRole] = useState<'EDITOR' | 'VIEWER'>('EDITOR');
  const [isInviting, setIsInviting] = useState<boolean>(false);
  
  // Modal State for Confirmations
  const [confirmModal, setConfirmModal] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    onConfirm: () => Promise<void>;
    isLoading: boolean;
  }>({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: async () => {},
    isLoading: false
  });

  // Edit State
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [editForm, setEditForm] = useState({
    title: '',
    description: '',
    destination: '',
    startDate: '',
    endDate: ''
  });

  // Permission Computations
  const isOwner = useMemo(() => {
    if (!trip || !authUser) return false;
    if (trip.ownerId && authUser.id) return trip.ownerId === authUser.id;
    if (trip.ownerEmail && authUser.email) return trip.ownerEmail.toLowerCase() === authUser.email.toLowerCase();
    const foundInMembers = members.some((m: any) => m.userId === authUser.id || m.email?.toLowerCase() === authUser.email?.toLowerCase());
    return !foundInMembers;
  }, [trip, authUser, members]);

  const currentUserMembership = useMemo(() => {
    if (!authUser || !members) return null;
    return members.find((m: any) => m.userId === authUser.id || m.email?.toLowerCase() === authUser.email?.toLowerCase());
  }, [members, authUser]);

  const currentUserRole: 'OWNER' | 'EDITOR' | 'VIEWER' = isOwner ? 'OWNER' : (currentUserMembership?.role || 'VIEWER');
  const canEdit = isOwner || currentUserRole === 'EDITOR';

  const fetchTripData = async () => {
    try {
      const tripResponse = await api.get(`/trips/${id}`);
      const fetchedTrip = tripResponse.data.data;
      setTrip(fetchedTrip);
      setEditForm({
        title: fetchedTrip.title || '',
        description: fetchedTrip.description || '',
        destination: fetchedTrip.destination || '',
        startDate: fetchedTrip.startDate || '',
        endDate: fetchedTrip.endDate || ''
      });
      
      // Fetch Weather Forecast for trip destination and dates
      if (fetchedTrip.destination && fetchedTrip.startDate && fetchedTrip.endDate) {
        try {
          const weatherRes = await WeatherControllerService.getWeather(
            fetchedTrip.destination,
            fetchedTrip.startDate,
            fetchedTrip.endDate
          );
          if (weatherRes.data?.dailyForecast) {
            const map: { [date: string]: any } = {};
            weatherRes.data.dailyForecast.forEach((item: any) => {
              if (item.date) map[item.date] = item;
            });
            setWeatherMap(map);
          }
        } catch (wErr) {
          console.warn("Weather forecast unavailable for these dates.");
        }
      }

      try {
        const itineraryResponse = await ItineraryControllerService.getItinerary(Number(id));
        setItinerary(itineraryResponse.data);
      } catch (itineraryErr) {
        console.warn("Itinerary not found or hasn't been generated yet.");
      }

      await fetchMembers();
    } catch (error) {
      console.error('Failed to fetch trip details', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchMembers = async () => {
    if (!id) return;
    setIsMembersLoading(true);
    try {
      const res = await api.get(`/trips/${id}/members`);
      setMembers(res.data.data || []);
    } catch (err) {
      console.warn('Could not fetch members:', err);
    } finally {
      setIsMembersLoading(false);
    }
  };

  useEffect(() => {
    fetchTripData();
  }, [id]);

  const handleEditChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
  };

  const handleSaveTrip = async () => {
    setIsSaving(true);
    try {
      await TripControllerService.updateMyTrip(Number(id), {
        title: editForm.title,
        description: editForm.description,
        destination: editForm.destination,
        startDate: editForm.startDate,
        endDate: editForm.endDate
      });
      toast.success('Trip updated successfully!');
      setIsEditing(false);
      fetchTripData(); // refresh data
    } catch (err: any) {
      toast.error(err.body?.message || 'Failed to update trip.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    const cleanEmail = inviteEmail.trim().toLowerCase();
    if (!cleanEmail) {
      toast.error('Please enter a user email address.');
      return;
    }

    if (authUser && cleanEmail === authUser.email?.toLowerCase()) {
      toast.error('You are already the owner of this trip.');
      return;
    }

    if (trip?.ownerEmail && cleanEmail === trip.ownerEmail.toLowerCase()) {
      toast.error('This user is already the trip organizer.');
      return;
    }

    if (members.some((m: any) => m.email?.toLowerCase() === cleanEmail)) {
      toast.error(`User '${inviteEmail}' is already a member of this trip.`);
      return;
    }

    setIsInviting(true);
    try {
      await api.post(`/trips/${id}/members`, {
        email: cleanEmail,
        role: inviteRole
      });
      toast.success(`Added ${cleanEmail} as ${inviteRole}!`);
      setInviteEmail('');
      await fetchMembers();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.body?.message || err.message || 'Failed to add member.';
      toast.error(msg);
    } finally {
      setIsInviting(false);
    }
  };

  const handleUpdateRole = async (memberId: number, currentRole: string) => {
    const nextRole = currentRole === 'EDITOR' ? 'VIEWER' : 'EDITOR';
    try {
      await api.put(`/trips/${id}/members/${memberId}/role?role=${nextRole}`);
      toast.success(`Updated role to ${nextRole}`);
      await fetchMembers();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.body?.message || err.message || 'Failed to update member role.';
      toast.error(msg);
    }
  };

  const handleRemoveMember = (member: any) => {
    const isSelf = member.userId === authUser?.id || member.email?.toLowerCase() === authUser?.email?.toLowerCase();
    const displayName = member.firstName ? `${member.firstName} ${member.lastName || ''}`.trim() : member.email;

    setConfirmModal({
      isOpen: true,
      title: isSelf ? 'Leave Trip' : 'Remove Member',
      message: isSelf
        ? 'Are you sure you want to leave this trip? You will lose access to its itinerary.'
        : `Are you sure you want to remove "${displayName}" from this trip?`,
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await api.delete(`/trips/${id}/members/${member.id}`);
          toast.success(isSelf ? 'You have left the trip.' : 'Member removed from trip.');
          setConfirmModal((prev) => ({ ...prev, isOpen: false }));
          if (isSelf) {
            navigate('/dashboard');
          } else {
            await fetchMembers();
          }
        } catch (err: any) {
          const msg = err.response?.data?.message || err.body?.message || err.message || 'Failed to remove member.';
          toast.error(msg);
        } finally {
          setConfirmModal((prev) => ({ ...prev, isLoading: false }));
        }
      }
    });
  };

  const openDeleteTripModal = () => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Trip',
      message: `Are you sure you want to delete "${trip.title}"? This action will permanently remove the trip and its entire itinerary schedule.`,
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await TripControllerService.deleteMyTrip(Number(id));
          toast.success('Trip deleted successfully');
          navigate('/dashboard');
        } catch (err: any) {
          toast.error(err.body?.message || 'Failed to delete trip');
          setConfirmModal((prev) => ({ ...prev, isLoading: false, isOpen: false }));
        }
      }
    });
  };

  const openDeleteActivityModal = (dayId: number, activityId: number, activityTitle: string) => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Activity',
      message: `Are you sure you want to remove "${activityTitle || 'this activity'}" from your itinerary?`,
      isLoading: false,
      onConfirm: async () => {
        setConfirmModal((prev) => ({ ...prev, isLoading: true }));
        try {
          await ItineraryControllerService.deleteActivity(Number(id), dayId, activityId);
          toast.success('Activity deleted');
          setConfirmModal((prev) => ({ ...prev, isOpen: false, isLoading: false }));
          fetchTripData(); // refresh itinerary
        } catch (err: any) {
          toast.error(err.body?.message || 'Failed to delete activity');
          setConfirmModal((prev) => ({ ...prev, isLoading: false, isOpen: false }));
        }
      }
    });
  };

  // Helper to resolve realistic activity cost if missing or zero
  const getActivityCost = (act: any): number => {
    const cost = Number(act?.estimatedCost);
    if (!isNaN(cost) && cost > 0) return cost;
    const type = (act?.type || '').toUpperCase();
    if (type === 'FOOD') return 300;
    if (type === 'SIGHTSEEING') return 200;
    return 150;
  };

  // Dynamically calculate activity cost from itinerary days & activities
  const dynamicActivityCost = React.useMemo(() => {
    if (!itinerary?.days) return 0;
    let sum = 0;
    itinerary.days.forEach((d: any) => {
      if (d.activities) {
        d.activities.forEach((a: any) => {
          sum += getActivityCost(a);
        });
      }
    });
    return sum;
  }, [itinerary]);

  // Dynamically calculate road distance and fuel cost across scheduled activities
  const dynamicRoadDistance = React.useMemo(() => {
    if (!itinerary?.days) return { distanceKm: 0, fuelCost: 0 };
    
    // Check if itinerary already has route data in routes
    if (itinerary.routes && itinerary.routes.length > 0) {
      const totalMeters = itinerary.routes.reduce((acc: number, r: any) => acc + (r.distanceMeters || 0), 0);
      const km = Math.round((totalMeters / 1000) * 10) / 10;
      const cost = Math.round(km * 14.0);
      return { distanceKm: km, fuelCost: cost };
    }

    // Count total activities to estimate driving distance between consecutive stops
    let count = 0;
    itinerary.days.forEach((d: any) => {
      if (d.activities) count += d.activities.length;
    });

    if (count < 2) return { distanceKm: 0, fuelCost: 0 };
    const km = Math.round((count - 1) * 14.5 * 10) / 10;
    const cost = Math.round(km * 14.0);
    return { distanceKm: km, fuelCost: cost };
  }, [itinerary]);

  const hotelCost = Number(itinerary?.estimatedHotelCost || 0);
  const activityCost = dynamicActivityCost > 0 ? dynamicActivityCost : Number(itinerary?.estimatedActivityCost || 0);
  const transportCost = (itinerary?.estimatedTransportCost && Number(itinerary.estimatedTransportCost) > 0)
    ? Number(itinerary.estimatedTransportCost)
    : dynamicRoadDistance.fuelCost;
  const finalTotalCost = hotelCost + activityCost + transportCost;

  if (isLoading) return <div className="text-center py-12">Loading trip details...</div>;
  if (!trip) return <div className="text-center py-12 text-red-500">Trip not found</div>;

  return (
    <div className="max-w-4xl mx-auto">
      <Link to="/dashboard" className="inline-flex items-center text-sm font-medium text-secondary-500 hover:text-primary-600 mb-6">
        <ArrowLeft className="mr-2 h-4 w-4" /> Back to Dashboard
      </Link>

      <div className="bg-white rounded-2xl shadow-sm border border-secondary-200 overflow-hidden">
        
        {isEditing ? (
          <div className="p-8 bg-secondary-50 border-b border-secondary-200">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-secondary-900">Edit Trip Details</h2>
              <button onClick={() => setIsEditing(false)} className="text-secondary-500 hover:text-secondary-700">
                <X className="h-6 w-6" />
              </button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-secondary-700 mb-1">Trip Title</label>
                <Input name="title" value={editForm.title} onChange={handleEditChange} />
              </div>
              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-secondary-700 mb-1">Destination</label>
                <Input name="destination" value={editForm.destination} onChange={handleEditChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1">Start Date</label>
                <Input type="date" name="startDate" value={editForm.startDate} onChange={handleEditChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-secondary-700 mb-1">End Date</label>
                <Input type="date" name="endDate" value={editForm.endDate} onChange={handleEditChange} />
              </div>
              <div className="col-span-1 md:col-span-2 mt-4 flex justify-end gap-3">
                <Button variant="outline" onClick={() => setIsEditing(false)}>Cancel</Button>
                <Button onClick={handleSaveTrip} disabled={isSaving}>
                  {isSaving ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : <Save className="h-4 w-4 mr-2" />}
                  Save Changes
                </Button>
              </div>
            </div>
          </div>
        ) : (
          <div className="min-h-[280px] sm:min-h-[320px] bg-secondary-200 relative overflow-hidden flex flex-col justify-end p-5 sm:p-8">
             <img 
               src={getTripCoverImage(trip)} 
               className="absolute inset-0 w-full h-full object-cover" 
               alt={trip.title || "Travel Cover"} 
             />
             <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/50 to-black/20"></div>
             <div className="relative z-10">
               <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                 <div>
                   <div className="flex flex-wrap items-center gap-2 mb-2 sm:mb-3">
                     <span className="inline-block px-3 py-1 bg-primary-500 text-white text-xs font-bold rounded-full uppercase tracking-wider shadow-xs">
                       {trip.status}
                     </span>
                     <span className={`inline-flex items-center gap-1 px-3 py-1 text-xs font-extrabold rounded-full backdrop-blur-md border shadow-xs ${
                       isOwner
                         ? 'bg-amber-500/90 text-white border-amber-300/40'
                         : currentUserRole === 'EDITOR'
                         ? 'bg-blue-500/90 text-white border-blue-300/40'
                         : 'bg-white/90 text-secondary-900 border-white/40'
                     }`}>
                       {isOwner ? <Crown className="h-3.5 w-3.5" /> : currentUserRole === 'EDITOR' ? <Pencil className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                       {currentUserRole}
                     </span>
                   </div>
                   <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-white mb-2 leading-tight">{trip.title}</h1>
                   <div className="flex flex-wrap items-center gap-3 sm:gap-4 text-white/90 text-xs sm:text-sm font-medium">
                     <span className="flex items-center gap-1.5"><MapPin className="h-3.5 w-3.5 sm:h-4 sm:w-4 text-primary-400 shrink-0" /> {trip.destination}</span>
                     <span className="flex items-center gap-1.5"><Calendar className="h-3.5 w-3.5 sm:h-4 sm:w-4 text-primary-400 shrink-0" /> {trip.startDate ? new Date(trip.startDate).toLocaleDateString() : 'TBD'} - {trip.endDate ? new Date(trip.endDate).toLocaleDateString() : 'TBD'}</span>
                   </div>
                 </div>

                 {/* Action Buttons guarded by Permissions */}
                 <div className="flex items-center gap-2 pt-2 sm:pt-0">
                   {canEdit && (
                     <Button 
                       variant="outline" 
                       className="bg-white/10 text-white border-white/30 hover:bg-white/20 text-xs font-semibold" 
                       onClick={() => setIsEditing(true)}
                     >
                       <Pencil className="h-3.5 w-3.5 mr-1.5" /> Edit Trip
                     </Button>
                   )}
                   {isOwner && (
                     <button 
                       onClick={openDeleteTripModal} 
                       className="p-2.5 bg-red-600/80 hover:bg-red-600 text-white rounded-lg transition-colors border border-red-500/30"
                       title="Delete Trip (Owner Only)"
                     >
                       <Trash2 className="h-4 w-4" />
                     </button>
                   )}
                   {!canEdit && (
                     <span className="text-xs px-3 py-1.5 bg-white/20 text-white rounded-full backdrop-blur-md flex items-center gap-1.5 font-semibold">
                       <Eye className="h-3.5 w-3.5" /> View-Only Access
                     </span>
                   )}
                 </div>
               </div>
             </div>
          </div>
        )}

        <div className="p-4 sm:p-6 lg:p-8">
          <h2 className="text-xl font-bold text-secondary-900 mb-6">Your Trip Itinerary</h2>
          
          {!itinerary || !itinerary.days || itinerary.days.length === 0 ? (
            <div className="bg-secondary-50 rounded-lg p-6 border border-secondary-100 text-center">
               <Clock className="h-8 w-8 text-secondary-400 mx-auto mb-3" />
               <h3 className="text-secondary-800 font-medium mb-1">No itinerary generated yet</h3>
               <p className="text-secondary-500 text-sm mb-4">You can manually build your itinerary or use AI.</p>
               <Link to="/ai-recommendations">
                 <Button variant="secondary" size="sm">Generate Itinerary</Button>
               </Link>
            </div>
          ) : (
            <div className="space-y-8">
              {/* Cost Breakdown Summary */}
              <div className="bg-gradient-to-br from-secondary-50 to-primary-50/40 rounded-2xl p-4 sm:p-5 border border-secondary-200/80 mb-6 sm:mb-8">
                <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                  <h3 className="text-xs font-bold text-secondary-500 uppercase tracking-wider">Trip Expense Breakdown</h3>
                  {isLocalCurrencyAvailable && (
                    <div className="inline-flex items-center bg-white p-0.5 rounded-lg border border-secondary-200 shadow-2xs text-xs">
                      <button
                        type="button"
                        onClick={() => setCurrencyMode('INR')}
                        className={`px-2.5 py-1 font-bold rounded-md transition-all ${
                          currencyMode === 'INR'
                            ? 'bg-secondary-900 text-white shadow-xs'
                            : 'text-secondary-500 hover:text-secondary-900'
                        }`}
                      >
                        ₹ INR
                      </button>
                      <button
                        type="button"
                        onClick={() => setCurrencyMode('LOCAL')}
                        className={`px-2.5 py-1 font-bold rounded-md transition-all flex items-center gap-1 ${
                          currencyMode === 'LOCAL'
                            ? 'bg-primary-600 text-white shadow-xs'
                            : 'text-secondary-500 hover:text-secondary-900'
                        }`}
                      >
                        <span>{localSymbol} {localCode}</span>
                        <span className="text-[10px] opacity-80">(Local)</span>
                      </button>
                    </div>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
                  <div className="bg-white p-3 sm:p-3.5 rounded-xl border border-secondary-100 shadow-sm flex sm:flex-col justify-between items-center sm:items-start">
                    <p className="text-xs text-secondary-500 flex items-center gap-1.5 sm:mb-1">
                      <Hotel className="h-3.5 w-3.5 text-primary-500" /> Accommodation
                    </p>
                    <p className="text-base sm:text-lg font-bold text-secondary-900">
                      {formatPrice(hotelCost)}
                    </p>
                  </div>

                  <div className="bg-white p-3 sm:p-3.5 rounded-xl border border-secondary-100 shadow-sm flex sm:flex-col justify-between items-center sm:items-start">
                    <p className="text-xs text-secondary-500 flex items-center gap-1.5 sm:mb-1">
                      <MapPin className="h-3.5 w-3.5 text-primary-500" /> Activities
                    </p>
                    <p className="text-base sm:text-lg font-bold text-secondary-900">
                      {formatPrice(activityCost)}
                    </p>
                  </div>

                  <div className="bg-white p-3 sm:p-3.5 rounded-xl border border-secondary-100 shadow-sm flex sm:flex-col justify-between items-center sm:items-start">
                    <div>
                      <p className="text-xs text-blue-600 font-medium flex items-center gap-1.5 sm:mb-1">
                        <Car className="h-3.5 w-3.5 text-blue-500" /> Road Distance
                      </p>
                      {dynamicRoadDistance.distanceKm > 0 && (
                        <p className="text-[10px] text-secondary-500 hidden sm:block mt-0.5">~{dynamicRoadDistance.distanceKm} km route</p>
                      )}
                    </div>
                    <p className="text-base sm:text-lg font-bold text-blue-900">
                      {formatPrice(transportCost)}
                    </p>
                  </div>

                  <div className="bg-primary-600 p-3 sm:p-3.5 rounded-xl text-white shadow-sm flex sm:flex-col justify-between items-center sm:items-start">
                    <p className="text-xs text-primary-100 sm:mb-1 font-medium">Final Total Amount</p>
                    <p className="text-lg sm:text-xl font-black text-white">
                      {formatPrice(finalTotalCost)}
                    </p>
                  </div>
                </div>
              </div>

              {itinerary.days.map((day: any, index: number) => {
                const dayWeather = day.date ? weatherMap[day.date] : null;
                const isRainy = dayWeather?.rainExpected || dayWeather?.condition?.toLowerCase().includes('rain');
                const isSunny = dayWeather?.condition?.toLowerCase().includes('sun') || dayWeather?.condition?.toLowerCase().includes('clear');

                return (
                  <div key={day.id || index} className="relative pl-8 border-l-2 border-primary-200">
                    <div className="absolute -left-3 top-0 h-6 w-6 rounded-full bg-primary-100 border-2 border-primary-500 flex items-center justify-center text-primary-700 font-bold text-xs">
                      {day.dayNumber}
                    </div>
                    
                    <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                      <h3 className="text-lg font-bold text-secondary-900">
                        Day {day.dayNumber} {day.date && <span className="text-sm font-medium text-secondary-500 ml-2">{new Date(day.date).toLocaleDateString()}</span>}
                      </h3>

                      {dayWeather && (
                        <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold shadow-sm ${
                          isRainy 
                            ? 'bg-blue-50 text-blue-700 border border-blue-200' 
                            : isSunny 
                            ? 'bg-amber-50 text-amber-700 border border-amber-200' 
                            : 'bg-secondary-100 text-secondary-700'
                        }`}>
                          {isRainy ? (
                            <CloudRain className="h-3.5 w-3.5 text-blue-500" />
                          ) : isSunny ? (
                            <Sun className="h-3.5 w-3.5 text-amber-500" />
                          ) : (
                            <Cloud className="h-3.5 w-3.5 text-secondary-500" />
                          )}
                          <span>
                            {isRainy ? 'Rain Expected 🌧️' : isSunny ? 'Sunny / Shiny ☀️' : (dayWeather.condition || 'Cloudy ⛅')}
                          </span>
                          {dayWeather.temperatureMax != null && (
                            <span className="opacity-80 font-normal">
                              ({Math.round(dayWeather.temperatureMax)}°C / {Math.round(dayWeather.temperatureMin)}°C)
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  
                  {day.activities && day.activities.length > 0 ? (
                    <div className="mt-4 space-y-4">
                      {day.activities.map((activity: any, aIndex: number) => {
                        if (!activity) return null;
                        return (
                          <div key={activity.id || aIndex} className="bg-white border border-secondary-200 rounded-xl p-4 shadow-sm relative group">
                            <div className="flex flex-col sm:flex-row justify-between sm:items-start gap-3">
                              <div className="flex-1">
                                <h4 className="font-bold text-secondary-900 text-sm sm:text-base">{activity?.title || 'Unknown Activity'}</h4>
                                <p className="text-xs sm:text-sm text-secondary-600 mt-1 leading-relaxed">{activity?.description}</p>
                                {activity?.location && (
                                  <p className="text-xs text-secondary-500 mt-2 flex items-center gap-1">
                                    <MapPin className="h-3 w-3 text-primary-500 shrink-0" /> {activity.location}
                                  </p>
                                )}
                              </div>
                              <div className="flex items-center justify-between sm:justify-end gap-3 pt-2 sm:pt-0 border-t sm:border-t-0 border-secondary-100 shrink-0">
                                <div>
                                  <span className="inline-block bg-secondary-100 text-secondary-700 text-xs font-semibold px-2 py-0.5 rounded">
                                    {activity?.startTime ? activity.startTime.substring(0,5) : '--:--'} - {activity?.endTime ? activity.endTime.substring(0,5) : '--:--'}
                                  </span>
                                  <p className="text-xs font-bold text-primary-600 sm:text-right mt-1">
                                    {formatPrice(getActivityCost(activity))}
                                  </p>
                                </div>
                                {activity?.id && (
                                  <button
                                    onClick={() => openDeleteActivityModal(day.id, activity.id, activity.title)}
                                    className="p-1.5 text-secondary-400 hover:text-red-600 rounded-md hover:bg-secondary-100 transition-colors"
                                    title="Delete Activity"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </button>
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <p className="text-sm text-secondary-500 mt-2 italic">Free day to explore on your own!</p>
                  )}
                </div>
              );
            })}
            </div>
          )}
        </div>

        {/* ========================================================= */}
        {/* Trip Members & Permission Collaboration Section */}
        {/* ========================================================= */}
        <div className="border-t border-secondary-200 bg-secondary-50/50 p-6 sm:p-8 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h2 className="text-xl font-bold text-secondary-900 flex items-center gap-2">
                <Users className="h-5 w-5 text-primary-600" />
                Trip Members & Collaboration
              </h2>
              <p className="text-xs text-secondary-500 mt-0.5">
                Manage traveler access permissions according to the OdysseyAI permission model.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-xs font-bold px-3 py-1 bg-white border border-secondary-200 rounded-full text-secondary-700 shadow-2xs">
                Your Role: <strong className={isOwner ? 'text-amber-600' : currentUserRole === 'EDITOR' ? 'text-primary-600' : 'text-emerald-600'}>{currentUserRole}</strong>
              </span>
            </div>
          </div>

          {/* If current user is VIEWER, display helpful banner */}
          {!canEdit && (
            <div className="bg-amber-50 border border-amber-200 rounded-xl p-3.5 flex items-center gap-3 text-xs text-amber-900">
              <Eye className="h-4 w-4 text-amber-600 shrink-0" />
              <span>
                You are viewing this trip with <strong>VIEWER</strong> permissions (read-only). You can explore the itinerary, but editing is restricted to the Trip Owner and Editors.
              </span>
            </div>
          )}

          {/* Owner Area: Invite / Add Member Form */}
          {isOwner && (
            <div className="bg-white rounded-2xl border border-secondary-200 p-5 shadow-xs space-y-4">
              <div className="flex items-center gap-2.5 pb-3 border-b border-secondary-100">
                <div className="p-2 bg-primary-50 text-primary-600 rounded-lg">
                  <UserPlus className="h-4 w-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-secondary-900">Add New Trip Member</h3>
                  <p className="text-xs text-secondary-500">Invite a fellow traveler by email and assign their role.</p>
                </div>
              </div>

              <form onSubmit={handleAddMember} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
                  {/* Email Input */}
                  <div className="sm:col-span-7">
                    <label className="block text-xs font-semibold text-secondary-700 mb-1">
                      User Email Address
                    </label>
                    <div className="relative">
                      <Mail className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-secondary-400" />
                      <Input
                        type="email"
                        required
                        placeholder="e.g. traveler@example.com"
                        value={inviteEmail}
                        onChange={(e) => setInviteEmail(e.target.value)}
                        className="pl-9 text-xs"
                      />
                    </div>
                  </div>

                  {/* Role Selection */}
                  <div className="sm:col-span-3">
                    <label className="block text-xs font-semibold text-secondary-700 mb-1">
                      Permission Role
                    </label>
                    <select
                      value={inviteRole}
                      onChange={(e) => setInviteRole(e.target.value as any)}
                      className="w-full bg-white border border-secondary-300 text-secondary-800 rounded-md px-3 py-2 text-xs focus:ring-primary-500 focus:border-primary-500 shadow-2xs font-medium"
                    >
                      <option value="EDITOR">EDITOR (Can Edit)</option>
                      <option value="VIEWER">VIEWER (View Only)</option>
                    </select>
                  </div>

                  {/* Submit Button */}
                  <div className="sm:col-span-2 flex items-end">
                    <Button
                      type="submit"
                      size="sm"
                      fullWidth
                      disabled={isInviting || !inviteEmail.trim()}
                      className="h-[38px] text-xs font-semibold flex items-center justify-center gap-1.5"
                    >
                      {isInviting ? (
                        <>
                          <Loader2 className="h-3.5 w-3.5 animate-spin" /> Adding...
                        </>
                      ) : (
                        <>
                          <UserPlus className="h-3.5 w-3.5" /> Add Member
                        </>
                      )}
                    </Button>
                  </div>
                </div>

                {/* Role Explanations */}
                <div className="flex flex-wrap items-center gap-4 pt-1 text-[11px] text-secondary-500">
                  <span className="flex items-center gap-1">
                    <Pencil className="h-3 w-3 text-primary-500" />
                    <strong>EDITOR:</strong> Can modify itinerary, activities & expenses.
                  </span>
                  <span className="flex items-center gap-1">
                    <Eye className="h-3 w-3 text-emerald-500" />
                    <strong>VIEWER:</strong> Read-only access to view the trip.
                  </span>
                </div>
              </form>
            </div>
          )}

          {/* Members Roster List */}
          <div className="bg-white rounded-2xl border border-secondary-200 overflow-hidden shadow-xs">
            <div className="p-4 bg-secondary-50/70 border-b border-secondary-100 flex items-center justify-between">
              <span className="text-xs font-bold text-secondary-700 uppercase tracking-wider flex items-center gap-1.5">
                <Users className="h-3.5 w-3.5 text-secondary-500" />
                Trip Roster ({1 + members.length} Total)
              </span>
            </div>

            <div className="divide-y divide-secondary-100">
              {/* Trip Owner Row */}
              <div className="p-4 flex items-center justify-between hover:bg-secondary-50/40 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-full bg-amber-100 text-amber-800 font-extrabold flex items-center justify-center text-xs shadow-2xs border border-amber-200">
                    <Crown className="h-4 w-4" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-secondary-900 flex items-center gap-2">
                      {trip.ownerName || (isOwner ? authUser?.name : 'Trip Organizer')}
                      {isOwner && (
                        <span className="text-[10px] px-1.5 py-0.2 bg-primary-100 text-primary-700 font-bold rounded">
                          You
                        </span>
                      )}
                    </div>
                    <span className="text-[11px] text-secondary-500">
                      {trip.ownerEmail || (isOwner ? authUser?.email : 'Organizer')}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-800 border border-amber-200">
                    <Crown className="h-3 w-3" /> TRIP OWNER (Full Control)
                  </span>
                </div>
              </div>

              {/* Added Members Rows */}
              {isMembersLoading ? (
                <div className="py-6 text-center text-xs text-secondary-500 flex items-center justify-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin text-primary-600" /> Loading members...
                </div>
              ) : members.length === 0 ? (
                <div className="py-6 text-center text-xs text-secondary-400">
                  No other members have been added to this trip yet.
                </div>
              ) : (
                members.map((member: any) => {
                  const isCurrentMember = member.userId === authUser?.id || member.email?.toLowerCase() === authUser?.email?.toLowerCase();
                  const name = member.firstName ? `${member.firstName} ${member.lastName || ''}`.trim() : member.email;

                  return (
                    <div key={member.id} className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-secondary-50/40 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-secondary-100 text-secondary-700 font-bold flex items-center justify-center text-xs shadow-2xs border border-secondary-200">
                          {member.firstName ? member.firstName.charAt(0).toUpperCase() : (member.email ? member.email.charAt(0).toUpperCase() : 'U')}
                        </div>
                        <div>
                          <div className="text-xs font-bold text-secondary-900 flex items-center gap-2">
                            {name}
                            {isCurrentMember && (
                              <span className="text-[10px] px-1.5 py-0.2 bg-primary-100 text-primary-700 font-bold rounded">
                                You
                              </span>
                            )}
                          </div>
                          <span className="text-[11px] text-secondary-500">{member.email}</span>
                        </div>
                      </div>

                      <div className="flex items-center gap-2.5 self-end sm:self-auto">
                        {/* Role Badge */}
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold border ${
                          member.role === 'EDITOR'
                            ? 'bg-blue-50 text-blue-700 border-blue-200'
                            : 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        }`}>
                          {member.role === 'EDITOR' ? <Pencil className="h-3 w-3" /> : <Eye className="h-3 w-3" />}
                          {member.role}
                        </span>

                        {/* Owner Controls: Toggle Role & Remove */}
                        {isOwner && (
                          <div className="flex items-center gap-1.5 border-l border-secondary-200 pl-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleUpdateRole(member.id, member.role)}
                              className="text-[11px] h-7 px-2"
                              title={`Switch to ${member.role === 'EDITOR' ? 'VIEWER' : 'EDITOR'}`}
                            >
                              Make {member.role === 'EDITOR' ? 'VIEWER' : 'EDITOR'}
                            </Button>
                            <button
                              onClick={() => handleRemoveMember(member)}
                              className="p-1.5 text-secondary-400 hover:text-red-600 rounded-md hover:bg-red-50 transition-colors"
                              title="Remove member from trip"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        )}

                        {/* Non-owner Member Control: Leave Trip */}
                        {!isOwner && isCurrentMember && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleRemoveMember(member)}
                            className="text-[11px] h-7 px-2 text-red-600 border-red-200 hover:bg-red-50"
                          >
                            Leave Trip
                          </Button>
                        )}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Graceful Centered Confirmation Modal */}
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={confirmModal.title}
        message={confirmModal.message}
        isLoading={confirmModal.isLoading}
        onConfirm={confirmModal.onConfirm}
        onClose={() => setConfirmModal((prev) => ({ ...prev, isOpen: false }))}
      />
    </div>
  );
};

export default TripDetails;
