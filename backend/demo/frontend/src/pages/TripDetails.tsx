import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { MapPin, Calendar, Clock, ArrowLeft } from 'lucide-react';
import api from '../api/axiosConfig';
import Button from '../components/ui/Button';

const TripDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [trip, setTrip] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTrip = async () => {
      try {
        const response = await api.get(`/trips/${id}`);
        setTrip(response.data.data);
      } catch (error) {
        console.error('Failed to fetch trip details', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchTrip();
  }, [id]);

  if (isLoading) return <div className="text-center py-12">Loading trip details...</div>;
  if (!trip) return <div className="text-center py-12 text-red-500">Trip not found</div>;

  return (
    <div className="max-w-4xl mx-auto">
      <Link to="/dashboard" className="inline-flex items-center text-sm font-medium text-secondary-500 hover:text-primary-600 mb-6">
        <ArrowLeft className="mr-2 h-4 w-4" /> Back to Dashboard
      </Link>

      <div className="bg-white rounded-2xl shadow-sm border border-secondary-200 overflow-hidden">
        <div className="h-64 bg-primary-100 relative">
           <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
           <div className="absolute bottom-6 left-8 right-8">
             <div className="flex justify-between items-end">
               <div>
                 <span className="inline-block px-3 py-1 bg-primary-500 text-white text-xs font-bold rounded-full mb-3 uppercase tracking-wider">
                   {trip.status}
                 </span>
                 <h1 className="text-3xl md:text-4xl font-bold text-white mb-2">{trip.title}</h1>
                 <div className="flex flex-wrap items-center gap-4 text-white/90 text-sm font-medium">
                   <span className="flex items-center gap-1.5"><MapPin className="h-4 w-4" /> {trip.destination}</span>
                   <span className="flex items-center gap-1.5"><Calendar className="h-4 w-4" /> {trip.startDate ? new Date(trip.startDate).toLocaleDateString() : 'TBD'}</span>
                 </div>
               </div>
               <div className="hidden sm:block">
                 <Button variant="primary">Edit Trip</Button>
               </div>
             </div>
           </div>
        </div>

        <div className="p-8">
          <h2 className="text-xl font-bold text-secondary-900 mb-4">Trip Itinerary Overview</h2>
          <div className="bg-secondary-50 rounded-lg p-6 border border-secondary-100 text-center">
             <Clock className="h-8 w-8 text-secondary-400 mx-auto mb-3" />
             <h3 className="text-secondary-800 font-medium mb-1">Itinerary details will appear here</h3>
             <p className="text-secondary-500 text-sm mb-4">Your daily activities and plans.</p>
             <Button variant="secondary" size="sm">Manage Itinerary</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TripDetails;
