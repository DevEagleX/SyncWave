import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getToken } from "../utils/tokenStorage";

let client = null;

export function connectSocket(onConnected) {
  client = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
    connectHeaders: {
      Authorization: `Bearer ${getToken()}`,
    },
    reconnectDelay: 5000,
    onConnect: () => {
      if (onConnected) onConnected();
    },
  });

  client.activate();
  return client;
}

export function disconnectSocket() {
  if (client) {
    client.deactivate();
    client = null;
  }
}

export function isSocketConnected() {
  return !!client && client.connected;
}

export function subscribeToConversation(conversationId, onMessage) {
  if (!client || !client.connected) return () => {};
  const sub = client.subscribe(`/topic/conversations/${conversationId}`, (message) => {
    onMessage(JSON.parse(message.body));
  });
  return () => sub.unsubscribe();
}

export function subscribeToTyping(conversationId, onEvent) {
  if (!client || !client.connected) return () => {};
  const sub = client.subscribe(`/topic/conversations/${conversationId}/typing`, (message) => {
    onEvent(JSON.parse(message.body));
  });
  return () => sub.unsubscribe();
}

export function subscribeToReadReceipts(conversationId, onEvent) {
  if (!client || !client.connected) return () => {};
  const sub = client.subscribe(`/topic/conversations/${conversationId}/read`, (message) => {
    onEvent(JSON.parse(message.body));
  });
  return () => sub.unsubscribe();
}

export function subscribeToPresence(onEvent) {
  if (!client || !client.connected) return () => {};
  const sub = client.subscribe("/topic/presence", (message) => {
    onEvent(JSON.parse(message.body));
  });
  return () => sub.unsubscribe();
}

export function sendChatMessage(conversationId, content) {
  if (!client || !client.connected) return;
  client.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({ conversationId, content }),
  });
}

export function sendTyping(conversationId, typing) {
  if (!client || !client.connected) return;
  client.publish({
    destination: "/app/chat.typing",
    body: JSON.stringify({ conversationId, typing }),
  });
}

export function sendReadReceipt(conversationId) {
  if (!client || !client.connected) return;
  client.publish({
    destination: "/app/chat.read",
    body: JSON.stringify({ conversationId }),
  });
}
