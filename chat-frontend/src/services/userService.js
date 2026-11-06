import api from "./api";

export const updateProfile = (data) => api.put("/users/me", data);
export const searchUsers = (query) => api.get(`/users/search?query=${encodeURIComponent(query)}`);
