import api from "./api";

export const startPrivateConversation = (friendId) => api.post(`/conversations/private/${friendId}`);
export const createGroupConversation = (name, participantIds) =>
  api.post("/conversations/group", { name, participantIds });
export const getConversations = () => api.get("/conversations");
export const getMessages = (conversationId) => api.get(`/conversations/${conversationId}/messages`);
export const markConversationRead = (conversationId) => api.put(`/conversations/${conversationId}/read`);
