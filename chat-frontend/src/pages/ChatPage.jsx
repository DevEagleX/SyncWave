import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getConversations } from "../services/conversationService";
import { usePresence } from "../context/PresenceContext";
import { subscribeToConversation } from "../websocket/socket";
import ConversationList from "../components/ConversationList";
import ChatWindow from "../components/ChatWindow";

export default function ChatPage() {
  const [conversations, setConversations] = useState([]);
  const { connected } = usePresence();
  const { conversationId } = useParams();

  const refreshConversations = () => {
    getConversations().then((res) => setConversations(res.data));
  };

  useEffect(() => {
    refreshConversations();
  }, []);

  // ChatWindow only listens to the ONE conversation currently open. This effect listens to
  // every conversation the user has, so the sidebar's preview/unread badge updates live even
  // for chats you're not currently looking at - no reload needed.
  useEffect(() => {
    if (!connected || conversations.length === 0) return;

    const unsubscribers = conversations.map((c) =>
      subscribeToConversation(c.id, refreshConversations)
    );

    return () => unsubscribers.forEach((unsub) => unsub());
    // Depend on the sorted list of ids (not the array itself) so re-sorting by last message
    // time doesn't tear down and rebuild every subscription on every incoming message.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [connected, conversations.map((c) => c.id).sort().join(",")]);

  const activeConversation = conversations.find((c) => String(c.id) === conversationId);
  const isGroup = activeConversation?.type === "GROUP";

  return (
    <div className="flex" style={{ height: "calc(100vh - 57px)" }}>
      <ConversationList conversations={conversations} />
      {connected ? <ChatWindow onActivity={refreshConversations} isGroup={isGroup} /> : (
        <div className="flex-1 flex items-center justify-center text-gray-400">Connecting...</div>
      )}
    </div>
  );
}
