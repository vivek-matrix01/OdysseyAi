import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, MapPin, Calendar, Users } from 'lucide-react';
import api from '../api/axiosConfig';
import Button from '../components/ui/Button';

interface Trip {
  id: number;
  title: string;
  description?: string;
  destination: string;
  startDate: string;
  endDate: string;
  status: string;
}

const Dashboard: React.FC = () => {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        const response = await api.get('/trips');
        // The API returns ApiResponse<PageResponse<TripResponse>>
        // so the actual array is inside response.data.data.content
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
    return <div className="text-center py-12">Loading your adventures...</div>;
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-secondary-900">Your Trips</h1>
          <p className="text-secondary-500">Manage and view all your upcoming and past adventures.</p>
        </div>
        <Link to="/ai-recommendations">
          <Button className="flex items-center gap-2">
            <Plus className="h-4 w-4" /> Plan New Trip
          </Button>
        </Link>
      </div>

      {trips.length === 0 ? (
        <div className="bg-white border border-secondary-200 rounded-xl p-12 text-center">
          <div className="bg-primary-50 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
            <MapPin className="h-8 w-8 text-primary-600" />
          </div>
          <h3 className="text-lg font-medium text-secondary-900 mb-2">No trips planned yet</h3>
          <p className="text-secondary-500 mb-6 max-w-md mx-auto">
            Ready to explore? Use our AI travel planner to generate an amazing itinerary for your next vacation.
          </p>
          <Link to="/ai-recommendations">
            <Button>Start Planning</Button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {trips.map((trip) => (
            <Link key={trip.id} to={`/trips/${trip.id}`} className="group">
              <div className="bg-white rounded-xl shadow-sm border border-secondary-200 overflow-hidden hover:shadow-md transition-shadow h-full flex flex-col">
                <div className="h-32 bg-primary-100 relative">
                  {/* Placeholder for trip image */}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                  <div className="absolute bottom-4 left-4 right-4">
                    <h3 className="text-lg font-bold text-white truncate">{trip.title}</h3>
                  </div>
                </div>
                <div className="p-5 flex-grow flex flex-col gap-3">
                  <div className="flex items-center text-sm text-secondary-600 gap-2">
                    <MapPin className="h-4 w-4 text-primary-500" />
                    <span>{trip.destination}</span>
                  </div>
                  <div className="flex items-center text-sm text-secondary-600 gap-2">
                    <Calendar className="h-4 w-4 text-primary-500" />
                    <span>
                      {trip.startDate ? new Date(trip.startDate).toLocaleDateString() : 'TBD'} - 
                      {trip.endDate ? new Date(trip.endDate).toLocaleDateString() : ' TBD'}
                    </span>
                  </div>
                  <div className="mt-auto pt-4 flex justify-between items-center">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary-100 text-primary-800">
                      {trip.status}
                    </span>
                    <span className="text-sm font-medium text-primary-600 group-hover:text-primary-700">
                      View Details &rarr;
                    </span>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
