import api from './axiosConfig';

export const AuthService = {
  login: (data: any) => api.post('/auth/login', data),
  register: (data: any) => api.post('/auth/register', data),
  refresh: (data: any) => api.post('/auth/refresh', data),
  logout: () => api.post('/auth/logout'),
  verifyEmail: (token: string) => api.get(`/auth/verify-email?token=${token}`),
  forgotPassword: (data: any) => api.post('/auth/forgot-password', data),
  resetPassword: (data: any) => api.post('/auth/reset-password', data),
};

export const UserService = {
  getCurrentProfile: () => api.get('/users/me'),
  updateProfile: (data: any) => api.put('/users/me', data),
  changePassword: (data: any) => api.put('/users/me/change-password', data),
};

export const AdminService = {
  getUsers: () => api.get('/admin/users'),
  getUserById: (id: number) => api.get(`/admin/users/${id}`),
  updateRole: (id: number, role: string) => api.put(`/admin/users/${id}/role`, { role }),
  lockUser: (id: number) => api.put(`/admin/users/${id}/lock`),
  unlockUser: (id: number) => api.put(`/admin/users/${id}/unlock`),
  deleteUser: (id: number) => api.delete(`/admin/users/${id}`),
};

export const AiService = {
  generateItinerary: (tripId: number, data: any) => api.post(`/ai/trips/${tripId}/itinerary/generate`, data),
  fromSelection: (data: any) => api.post('/ai/trips/itinerary/from-selection', data),
  getRecommendations: (data: any) => api.post('/ai/recommendations', data),
  confirmRecommendation: (data: any) => api.post('/ai/recommendations/confirm', data),
};

export const TripService = {
  createTrip: (data: any) => api.post('/trips', data),
  getMyTrips: (params?: any) => api.get('/trips', { params }),
  getTripById: (id: number) => api.get(`/trips/${id}`),
  updateTrip: (id: number, data: any) => api.put(`/trips/${id}`, data),
  deleteTrip: (id: number) => api.delete(`/trips/${id}`),
  startTrip: (id: number) => api.post(`/trips/${id}/start`),
  completeTrip: (id: number) => api.post(`/trips/${id}/complete`),
  cancelTrip: (id: number) => api.post(`/trips/${id}/cancel`),
};

export const TripMemberService = {
  addMember: (tripId: number, data: any) => api.post(`/trips/${tripId}/members`, data),
  getMembers: (tripId: number) => api.get(`/trips/${tripId}/members`),
  updateMemberRole: (tripId: number, memberId: number, data: any) => api.put(`/trips/${tripId}/members/${memberId}/role`, data),
  removeMember: (tripId: number, memberId: number) => api.delete(`/trips/${tripId}/members/${memberId}`),
};

export const ItineraryService = {
  createItinerary: (tripId: number, data: any) => api.post(`/trips/${tripId}/itinerary`, data),
  getItinerary: (tripId: number) => api.get(`/trips/${tripId}/itinerary`),
  updateItinerary: (tripId: number, data: any) => api.put(`/trips/${tripId}/itinerary`, data),
  addDay: (tripId: number, data: any) => api.post(`/trips/${tripId}/itinerary/days`, data),
  updateDay: (tripId: number, dayId: number, data: any) => api.put(`/trips/${tripId}/itinerary/days/${dayId}`, data),
  deleteDay: (tripId: number, dayId: number) => api.delete(`/trips/${tripId}/itinerary/days/${dayId}`),
  addActivity: (tripId: number, dayId: number, data: any) => api.post(`/trips/${tripId}/itinerary/days/${dayId}/activities`, data),
  updateActivity: (tripId: number, dayId: number, activityId: number, data: any) => api.put(`/trips/${tripId}/itinerary/days/${dayId}/activities/${activityId}`, data),
  deleteActivity: (tripId: number, dayId: number, activityId: number) => api.delete(`/trips/${tripId}/itinerary/days/${dayId}/activities/${activityId}`),
};

export const ExpenseService = {
  addExpense: (tripId: number, data: any) => api.post(`/trips/${tripId}/expenses`, data),
  getExpenses: (tripId: number, params?: any) => api.get(`/trips/${tripId}/expenses`, { params }),
  updateExpense: (tripId: number, expenseId: number, data: any) => api.put(`/trips/${tripId}/expenses/${expenseId}`, data),
  deleteExpense: (tripId: number, expenseId: number) => api.delete(`/trips/${tripId}/expenses/${expenseId}`),
};

export const TravelExtrasService = {
  getCostEstimate: (tripId: number, data: any) => api.post(`/trips/${tripId}/cost-estimate`, data),
  getWeather: (params: any) => api.get('/travel/weather', { params }),
};
