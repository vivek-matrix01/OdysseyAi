import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import api from '../api/axiosConfig';
import { Sparkles, Map, Calendar, Wallet } from 'lucide-react';

const AiRecommendation: React.FC = () => {
  const [formData, setFormData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    budget: '',
    interests: '',
    travelers: 1,
    tripType: 'Vacation'
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

      try {
      const recommendResponse = await api.post('/ai/recommendations', {
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
      
      const recommendation = recommendResponse.data.data;
      
      // Auto-confirm the trip creation so it appears in the dashboard
      await api.post('/ai/recommendations/confirm', {
        title: `Trip to ${formData.destination}`,
        description: recommendation.recommendationReason || 'AI Generated Trip',
        destination: formData.destination,
        startDate: formData.startDate,
        endDate: formData.endDate
      });

      toast.success('AI Itinerary generated and trip created successfully!');
      navigate('/dashboard'); 
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to generate recommendations. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

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
        <form onSubmit={handleSubmit} className="space-y-6">
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
                  <Sparkles className="h-5 w-5 animate-spin" /> Generating Magic...
                </span>
              ) : (
                'Generate Itinerary'
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AiRecommendation;
