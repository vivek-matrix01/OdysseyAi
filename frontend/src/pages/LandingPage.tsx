import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Sparkles, 
  MapPinned, 
  MapPin, 
  Hotel, 
  Car, 
  CloudSun, 
  Users, 
  ShieldCheck, 
  ArrowRight, 
  Clock, 
  Star, 
  Check,
  Sun,
  Eye,
  Pencil,
  Crown
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import BorderGlow from '../components/ui/BorderGlow';

// Curated showcase destinations with high-resolution Unsplash photography
const FEATURED_DESTINATIONS = [
  {
    id: 1,
    title: 'Goa Coastal Getaway',
    category: 'BEACH & RELAXATION',
    destination: 'Goa, India',
    days: '4-5 Days',
    rating: '4.9',
    reviews: '1.2k',
    image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=800&q=80',
    description: 'Golden sand beaches, Portuguese heritage churches, beachside shacks, and vibrant sunset cruises.'
  },
  {
    id: 2,
    title: 'Himalayan Ridge Explorer',
    category: 'ADVENTURE & MOUNTAINS',
    destination: 'Manali, Himachal Pradesh',
    days: '5-6 Days',
    rating: '4.8',
    reviews: '980',
    image: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80',
    description: 'Snow-clad peaks, Solang Valley alpine sports, hot springs, and serene pine forest trails.'
  },
  {
    id: 3,
    title: 'Royal Palace Odyssey',
    category: 'HERITAGE & CULTURE',
    destination: 'Jaipur, Rajasthan',
    days: '3-4 Days',
    rating: '4.9',
    reviews: '1.5k',
    image: 'https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=800&q=80',
    description: 'Amber Fort majesty, Hawa Mahal architecture, authentic Rajasthani dining, and lively bazaars.'
  },
  {
    id: 4,
    title: 'Tropical Backwaters Voyage',
    category: 'NATURE & WELLNESS',
    destination: 'Alleppey, Kerala',
    days: '3-4 Days',
    rating: '4.9',
    reviews: '850',
    image: 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80',
    description: 'Tranquil houseboat cruising, Ayurvedic retreats, coconut-fringed canals, and coastal sunsets.'
  },
  {
    id: 5,
    title: 'Neon Metropolis Adventure',
    category: 'CITY & GASTRONOMY',
    destination: 'Tokyo, Japan',
    days: '6-7 Days',
    rating: '5.0',
    reviews: '2.1k',
    image: 'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80',
    description: 'Futuristic skyscrapers, historic Asakusa shrines, world-class ramen, and Shibuya crossing energy.'
  },
  {
    id: 6,
    title: 'Parisian Romance & Arts',
    category: 'ROMANCE & ICONIC SIGHTS',
    destination: 'Paris, France',
    days: '5 Days',
    rating: '4.8',
    reviews: '1.9k',
    image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80',
    description: 'The Eiffel Tower at golden hour, Louvre art treasures, Seine river walks, and sidewalk cafes.'
  }
];

const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="flex flex-col min-h-screen">
      {/* ============================================================ */}
      {/* 1. HERO SECTION */}
      {/* ============================================================ */}
      <section className="relative overflow-hidden bg-gradient-to-b from-primary-900 via-secondary-900 to-secondary-950 text-white pt-16 pb-24 lg:pt-24 lg:pb-32">
        {/* Ambient Glows */}
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-primary-500/20 rounded-full blur-3xl pointer-events-none -translate-y-1/2"></div>
        <div className="absolute bottom-0 right-10 w-96 h-96 bg-accent-500/15 rounded-full blur-3xl pointer-events-none"></div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
            {/* Left Copy */}
            <div className="lg:col-span-7 space-y-6 text-center lg:text-left">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-primary-500/15 border border-primary-400/30 text-primary-300 text-xs font-semibold backdrop-blur-md">
                <Sparkles className="h-4 w-4 text-primary-300" />
                <span>Next-Gen Autonomous AI Travel Planning</span>
              </div>

              <h1 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-tight text-white">
                Turn Your Dream Vacations Into{' '}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary-300 via-primary-200 to-accent-300">
                  Flawless Itineraries
                </span>
              </h1>

              <p className="text-secondary-300 text-sm sm:text-lg max-w-2xl mx-auto lg:mx-0 leading-relaxed font-normal">
                Discover personalized attractions, verified hotels via SerpApi, live weather forecasts, automated distance & fuel estimations, and collaborate with your travel crew with granular Owner, Editor, and Viewer permissions.
              </p>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-3 sm:gap-4 pt-2">
                <Link to={isAuthenticated ? "/ai-recommendations" : "/register"} className="w-full sm:w-auto">
                  <Button size="lg" className="w-full sm:w-auto flex items-center justify-center gap-2 shadow-lg shadow-primary-600/30 font-bold px-6 sm:px-8 py-3.5 text-sm sm:text-base">
                    <span>{isAuthenticated ? "Open AI Planner" : "Start Planning Free"}</span>
                    <ArrowRight className="h-5 w-5" />
                  </Button>
                </Link>

                <Link to={isAuthenticated ? "/dashboard" : "/login"} className="w-full sm:w-auto">
                  <Button 
                    variant="outline" 
                    size="lg" 
                    className="w-full sm:w-auto bg-white/10 hover:bg-white/20 text-white border-white/20 backdrop-blur-md font-semibold px-6 py-3.5 text-sm sm:text-base"
                  >
                    <span>{isAuthenticated ? "My Trips Dashboard" : "Sign In to Account"}</span>
                  </Button>
                </Link>
              </div>

              {/* Highlights Micro-stats */}
              <div className="pt-6 border-t border-white/10 grid grid-cols-3 gap-2 sm:gap-4 text-center lg:text-left">
                <div>
                  <div className="text-xl sm:text-3xl font-extrabold text-white">20k+</div>
                  <div className="text-[10px] sm:text-xs text-secondary-400 font-medium mt-0.5">Places & Sights</div>
                </div>
                <div>
                  <div className="text-xl sm:text-3xl font-extrabold text-white">100%</div>
                  <div className="text-[10px] sm:text-xs text-secondary-400 font-medium mt-0.5">Real Road Routing</div>
                </div>
                <div>
                  <div className="text-xl sm:text-3xl font-extrabold text-white">3-Tier</div>
                  <div className="text-[10px] sm:text-xs text-secondary-400 font-medium mt-0.5">Role Collaboration</div>
                </div>
              </div>
            </div>

            {/* Right Interactive Mockup Card */}
            <div className="lg:col-span-5 relative">
              <BorderGlow
                edgeSensitivity={30}
                glowColor="40 80 80"
                backgroundColor="#120F17"
                borderRadius={28}
                glowRadius={40}
                glowIntensity={1}
                coneSpread={25}
                animated={true}
                colors={['#c084fc', '#f472b6', '#38bdf8']}
                className="relative mx-auto max-w-md shadow-2xl transform hover:-translate-y-1 transition-transform duration-500"
              >
                <div className="p-5 sm:p-6 text-white">
                  {/* Mock Card Header Image */}
                  <div className="relative h-48 rounded-2xl overflow-hidden mb-5">
                    <img 
                      src="https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80" 
                      alt="Bali Mockup"
                      className="w-full h-full object-cover" 
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/30 to-transparent"></div>
                    
                    <span className="absolute top-3 left-3 bg-black/60 backdrop-blur-md text-[11px] font-semibold px-2.5 py-1 rounded-full border border-white/20 flex items-center gap-1">
                      <Clock className="h-3 w-3 text-primary-400" /> 5 Days
                    </span>

                    <span className="absolute top-3 right-3 bg-emerald-500/90 text-[11px] font-bold px-2.5 py-1 rounded-full text-white shadow-xs">
                      AI OPTIMIZED
                    </span>

                    <div className="absolute bottom-3 left-4 right-4">
                      <h3 className="text-lg font-bold text-white leading-snug">Tropical Paradise Escape</h3>
                      <p className="text-xs text-secondary-200 flex items-center gap-1 mt-0.5">
                        <MapPin className="h-3.5 w-3.5 text-primary-400" /> Bali & Ubud, Indonesia
                      </p>
                    </div>
                  </div>

                  {/* Mock Metrics Row */}
                  <div className="grid grid-cols-3 gap-2.5 mb-5 text-center">
                    <div className="bg-white/10 rounded-xl p-2.5 border border-white/10">
                      <div className="text-[10px] text-secondary-300 flex items-center justify-center gap-1">
                        <Hotel className="h-3 w-3 text-primary-300" /> Stay
                      </div>
                      <div className="text-xs font-bold mt-1 text-white">₹4,200/n</div>
                    </div>

                    <div className="bg-white/10 rounded-xl p-2.5 border border-white/10">
                      <div className="text-[10px] text-secondary-300 flex items-center justify-center gap-1">
                        <Car className="h-3 w-3 text-amber-300" /> Road Fuel
                      </div>
                      <div className="text-xs font-bold mt-1 text-white">₹1,180</div>
                    </div>

                    <div className="bg-white/10 rounded-xl p-2.5 border border-white/10">
                      <div className="text-[10px] text-secondary-300 flex items-center justify-center gap-1">
                        <Sun className="h-3 w-3 text-amber-300" /> Weather
                      </div>
                      <div className="text-xs font-bold mt-1 text-white">29°C Sunny</div>
                    </div>
                  </div>

                  {/* Mock Collaborators Bar */}
                  <div className="bg-black/30 rounded-xl p-3 border border-white/10 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="flex -space-x-2">
                        <div className="w-7 h-7 rounded-full bg-amber-400 text-secondary-900 font-extrabold flex items-center justify-center text-[10px] border-2 border-secondary-900">
                          VK
                        </div>
                        <div className="w-7 h-7 rounded-full bg-blue-400 text-white font-extrabold flex items-center justify-center text-[10px] border-2 border-secondary-900">
                          SR
                        </div>
                        <div className="w-7 h-7 rounded-full bg-emerald-400 text-secondary-900 font-extrabold flex items-center justify-center text-[10px] border-2 border-secondary-900">
                          AN
                        </div>
                      </div>
                      <span className="text-[11px] text-secondary-300 font-medium">3 Collaborators</span>
                    </div>

                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-primary-500/20 text-primary-300 border border-primary-400/30 font-bold">
                      Owner & Editors
                    </span>
                  </div>
                </div>
              </BorderGlow>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================ */}
      {/* 2. CORE CAPABILITIES (FEATURES) */}
      {/* ============================================================ */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-extrabold text-primary-600 tracking-wider uppercase px-3 py-1 bg-primary-50 rounded-full border border-primary-100">
              Intelligence in Motion
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-secondary-900 mt-4 tracking-tight">
              Everything You Need for Effortless Journeys
            </h2>
            <p className="text-secondary-500 mt-3 text-base">
              OdysseyAi integrates search engines, geographical APIs, and machine learning to build transparent, cost-accurate travel schedules.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-primary-100 text-primary-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-primary-600 group-hover:text-white transition-all">
                  <Sparkles className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">AI-Powered Recommendation</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Tell OdysseyAi your destination, dates, budget, and travel preferences. Our AI model immediately crafts a tailored lineup of top tourist attractions and verified stays.
                </p>
              </div>
            </BorderGlow>

            {/* Feature 2 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-blue-600 group-hover:text-white transition-all">
                  <Hotel className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">SerpApi Live Hotel Rates</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Real-time hotel room pricing, high-res photos, star ratings, and backup image fallbacks. Never get surprised by unavailable stays or missing photos.
                </p>
              </div>
            </BorderGlow>

            {/* Feature 3 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-amber-600 group-hover:text-white transition-all">
                  <Car className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">Road Distance & Fuel Math</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Inter-activity driving distance calculated dynamically across chosen stops with real-time petrol and diesel estimates. No guesswork on transit overhead.
                </p>
              </div>
            </BorderGlow>

            {/* Feature 4 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-emerald-600 group-hover:text-white transition-all">
                  <CloudSun className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">Integrated Weather Forecast</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Live daily temperature, weather descriptions, and precipitation warnings directly in your daily planner so you always pack and schedule smartly.
                </p>
              </div>
            </BorderGlow>

            {/* Feature 5 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-purple-100 text-purple-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-purple-600 group-hover:text-white transition-all">
                  <Users className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">3-Tier Collaboration</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Invite friends and co-travelers via email. Assign them as <strong>Editors</strong> to collaborate on plans, or <strong>Viewers</strong> for read-only access.
                </p>
              </div>
            </BorderGlow>

            {/* Feature 6 */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#ffffff"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full group hover:shadow-xl transition-all duration-300"
            >
              <div className="p-7 flex flex-col h-full">
                <div className="w-12 h-12 rounded-xl bg-red-100 text-red-600 flex items-center justify-center mb-5 group-hover:scale-110 group-hover:bg-red-600 group-hover:text-white transition-all">
                  <ShieldCheck className="h-6 w-6" />
                </div>
                <h3 className="text-lg font-bold text-secondary-900 mb-2">Location Intelligence Guard</h3>
                <p className="text-sm text-secondary-600 leading-relaxed">
                  Automatic validation against Google Places and SerpApi. If a requested spot is unserviced, get courteous, clear notices without cryptic application crashes.
                </p>
              </div>
            </BorderGlow>
          </div>
        </div>
      </section>

      {/* ============================================================ */}
      {/* 3. HOW IT WORKS (3 SIMPLE STEPS) */}
      {/* ============================================================ */}
      <section className="py-20 bg-secondary-100/60 border-y border-secondary-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <span className="text-xs font-bold text-primary-600 uppercase tracking-wider px-3 py-1 bg-primary-100/70 rounded-full">
              Quick & Intuitive
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-secondary-900 mt-3 tracking-tight">
              Plan Your Adventure in 3 Steps
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
            {/* Step 1 */}
            <div className="bg-white rounded-2xl p-8 border border-secondary-200 shadow-sm relative z-10 flex flex-col items-center text-center">
              <div className="w-12 h-12 rounded-full bg-primary-600 text-white font-extrabold flex items-center justify-center text-lg mb-6 shadow-md shadow-primary-500/30">
                1
              </div>
              <h3 className="text-lg font-bold text-secondary-900 mb-2">Specify Your Trip</h3>
              <p className="text-sm text-secondary-600">
                Enter your destination, dates, budget, and travel preferences. OdysseyAi prepares a comprehensive catalog of authentic places.
              </p>
            </div>

            {/* Step 2 */}
            <div className="bg-white rounded-2xl p-8 border border-secondary-200 shadow-sm relative z-10 flex flex-col items-center text-center">
              <div className="w-12 h-12 rounded-full bg-primary-600 text-white font-extrabold flex items-center justify-center text-lg mb-6 shadow-md shadow-primary-500/30">
                2
              </div>
              <h3 className="text-lg font-bold text-secondary-900 mb-2">Pick & Customize</h3>
              <p className="text-sm text-secondary-600">
                Review verified SerpApi hotel options and categorized attractions. Select your favorite spots or let AI recommend the optimal selection.
              </p>
            </div>

            {/* Step 3 */}
            <div className="bg-white rounded-2xl p-8 border border-secondary-200 shadow-sm relative z-10 flex flex-col items-center text-center">
              <div className="w-12 h-12 rounded-full bg-primary-600 text-white font-extrabold flex items-center justify-center text-lg mb-6 shadow-md shadow-primary-500/30">
                3
              </div>
              <h3 className="text-lg font-bold text-secondary-900 mb-2">Generate & Collaborate</h3>
              <p className="text-sm text-secondary-600">
                Receive an hour-by-hour itinerary with fuel costs and weather alerts. Invite friends as Editors or Viewers to travel as a team.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ============================================================ */}
      {/* 4. FEATURED DESTINATIONS SHOWCASE */}
      {/* ============================================================ */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-4">
            <div>
              <span className="text-xs font-bold text-primary-600 uppercase tracking-wider px-3 py-1 bg-primary-50 rounded-full border border-primary-100">
                Popular Getaways
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-secondary-900 mt-3 tracking-tight">
                Trending Destinations Ready to Explore
              </h2>
              <p className="text-secondary-500 text-sm mt-1">
                Hand-picked places travelers are loving right now on OdysseyAi.
              </p>
            </div>

            <Link to={isAuthenticated ? "/ai-recommendations" : "/register"}>
              <Button variant="outline" className="flex items-center gap-2">
                <span>Plan Custom Destination</span>
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-7">
            {FEATURED_DESTINATIONS.map((dest) => (
              <BorderGlow
                key={dest.id}
                edgeSensitivity={30}
                glowColor="40 80 80"
                backgroundColor="#ffffff"
                borderRadius={20}
                glowRadius={40}
                glowIntensity={1}
                coneSpread={25}
                animated={false}
                colors={['#c084fc', '#f472b6', '#38bdf8']}
                className="group h-full shadow-xs hover:shadow-xl transition-all duration-300"
              >
                <div className="overflow-hidden flex flex-col h-full">
                  <div className="relative h-52 overflow-hidden bg-secondary-100">
                    <img 
                      src={dest.image} 
                      alt={dest.title} 
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ease-out" 
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
                    
                    <span className="absolute top-3 left-3 bg-black/60 backdrop-blur-md text-white text-[11px] font-semibold px-2.5 py-1 rounded-full border border-white/20">
                      {dest.days}
                    </span>

                    <span className="absolute top-3 right-3 bg-white/95 backdrop-blur-md text-secondary-900 text-[11px] font-bold px-2 py-0.5 rounded-full flex items-center gap-1 shadow-xs">
                      <Star className="h-3 w-3 text-amber-500 fill-amber-500" /> {dest.rating}
                    </span>

                    <div className="absolute bottom-3 left-4 right-4">
                      <span className="text-[10px] font-bold tracking-wider text-primary-300 uppercase">
                        {dest.category}
                      </span>
                      <h3 className="text-lg font-bold text-white group-hover:text-primary-200 transition-colors">
                        {dest.title}
                      </h3>
                    </div>
                  </div>

                  <div className="p-5 flex-grow flex flex-col justify-between gap-4">
                    <p className="text-xs text-secondary-600 leading-relaxed">
                      {dest.description}
                    </p>

                    <div className="pt-3 border-t border-secondary-100 flex items-center justify-between">
                      <span className="text-xs font-semibold text-secondary-500 flex items-center gap-1">
                        <MapPin className="h-3.5 w-3.5 text-primary-500" /> {dest.destination}
                      </span>

                      <Link to={isAuthenticated ? "/ai-recommendations" : "/register"}>
                        <span className="inline-flex items-center gap-1 text-xs font-bold text-primary-600 hover:text-primary-700 group-hover:translate-x-0.5 transition-all">
                          Plan Trip <ArrowRight className="h-3 w-3" />
                        </span>
                      </Link>
                    </div>
                  </div>
                </div>
              </BorderGlow>
            ))}
          </div>
        </div>
      </section>

      {/* ============================================================ */}
      {/* 5. TEAM PERMISSION MODEL SHOWCASE */}
      {/* ============================================================ */}
      <section className="py-20 bg-secondary-900 text-white relative overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-bold text-primary-300 uppercase tracking-wider px-3 py-1 bg-white/10 rounded-full border border-white/20">
              Built for Group Travel
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white mt-3 tracking-tight">
              Granular Role-Based Permissions
            </h2>
            <p className="text-secondary-300 mt-2 text-sm">
              Keep full ownership of your trips while collaborating with friends, family, and colleagues.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Owner Box */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#120F17"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              animated={false}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full shadow-xl"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="w-10 h-10 rounded-xl bg-amber-400/20 text-amber-300 flex items-center justify-center mb-4">
                  <Crown className="h-5 w-5" />
                </div>
                <span className="text-xs font-bold text-amber-300 uppercase tracking-wider">Top Authority</span>
                <h3 className="text-xl font-bold text-white mt-1 mb-2">Trip Owner</h3>
                <p className="text-xs text-secondary-300 mb-4 leading-relaxed">
                  Full administrative control. Can edit dates, modify itinerary, manage expenses, invite members, and delete the trip.
                </p>
                <ul className="space-y-2 text-xs text-secondary-200 mt-auto">
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-amber-400" /> Full Edit & Delete rights</li>
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-amber-400" /> Invite & Kick members</li>
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-amber-400" /> Change collaborator roles</li>
                </ul>
              </div>
            </BorderGlow>

            {/* Editor Box */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#120F17"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              animated={false}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full shadow-xl"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="w-10 h-10 rounded-xl bg-blue-400/20 text-blue-300 flex items-center justify-center mb-4">
                  <Pencil className="h-5 w-5" />
                </div>
                <span className="text-xs font-bold text-blue-300 uppercase tracking-wider">Collaborative Editor</span>
                <h3 className="text-xl font-bold text-white mt-1 mb-2">Editor</h3>
                <p className="text-xs text-secondary-300 mb-4 leading-relaxed">
                  Active co-planner. Can add, edit, or delete activities, schedule stops, and update travel expenses with the owner.
                </p>
                <ul className="space-y-2 text-xs text-secondary-200 mt-auto">
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-blue-400" /> Edit itinerary & stops</li>
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-blue-400" /> Log and update expenses</li>
                  <li className="flex items-center gap-2 text-secondary-400">Cannot delete trip or kick users</li>
                </ul>
              </div>
            </BorderGlow>

            {/* Viewer Box */}
            <BorderGlow
              edgeSensitivity={30}
              glowColor="40 80 80"
              backgroundColor="#120F17"
              borderRadius={20}
              glowRadius={40}
              glowIntensity={1}
              coneSpread={25}
              animated={false}
              colors={['#c084fc', '#f472b6', '#38bdf8']}
              className="h-full shadow-xl"
            >
              <div className="p-6 flex flex-col h-full">
                <div className="w-10 h-10 rounded-xl bg-emerald-400/20 text-emerald-300 flex items-center justify-center mb-4">
                  <Eye className="h-5 w-5" />
                </div>
                <span className="text-xs font-bold text-emerald-300 uppercase tracking-wider">Read-Only Participant</span>
                <h3 className="text-xl font-bold text-white mt-1 mb-2">Viewer</h3>
                <p className="text-xs text-secondary-300 mb-4 leading-relaxed">
                  Travel attendee. Can view the full day-by-day schedule, weather, and costs without modifying any data.
                </p>
                <ul className="space-y-2 text-xs text-secondary-200 mt-auto">
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-emerald-400" /> View full itinerary schedule</li>
                  <li className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-emerald-400" /> Check live weather & routes</li>
                  <li className="flex items-center gap-2 text-secondary-400">Read-only protection</li>
                </ul>
              </div>
            </BorderGlow>
          </div>
        </div>
      </section>

      {/* ============================================================ */}
      {/* 6. CALL TO ACTION (CTA) */}
      {/* ============================================================ */}
      <section className="py-20 bg-primary-600 text-white relative overflow-hidden">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
          <div className="inline-flex p-3 rounded-2xl bg-white/10 backdrop-blur-md mb-2">
            <MapPinned className="h-10 w-10 text-white" />
          </div>

          <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight">
            Ready for Your Next Unforgettable Journey?
          </h2>

          <p className="text-primary-100 max-w-2xl mx-auto text-base sm:text-lg">
            Join thousands of travelers who plan trips with zero hassle, accurate budgets, verified hotels, and AI speed.
          </p>

          <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to={isAuthenticated ? "/ai-recommendations" : "/register"} className="w-full sm:w-auto">
              <Button size="lg" className="w-full sm:w-auto bg-white text-primary-700 hover:bg-primary-50 font-bold px-8 py-3.5 shadow-xl text-base">
                <span>{isAuthenticated ? "Launch AI Planner" : "Create Free Account"}</span>
                <ArrowRight className="h-5 w-5 ml-2" />
              </Button>
            </Link>

            <Link to={isAuthenticated ? "/dashboard" : "/login"} className="w-full sm:w-auto">
              <Button variant="outline" size="lg" className="w-full sm:w-auto border-white/40 text-white hover:bg-white/10 font-semibold px-6 py-3.5 text-base">
                <span>{isAuthenticated ? "Go to Dashboard" : "Sign In"}</span>
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
