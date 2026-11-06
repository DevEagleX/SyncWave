import api from "./api";

export const sendFriendRequest = (receiverId) => api.post(`/friend-requests/${receiverId}`);
export const acceptFriendRequest = (requestId) => api.put(`/friend-requests/${requestId}/accept`);
export const rejectFriendRequest = (requestId) => api.put(`/friend-requests/${requestId}/reject`);
export const getIncomingRequests = () => api.get("/friend-requests/incoming");
export const getFriends = () => api.get("/friends");
