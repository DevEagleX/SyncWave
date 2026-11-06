import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { getMessages, markConversationRead } from "../services/conversationService";
import {
  subscribeToConversation,
  subscribeToTyping,
  subscribeToReadReceipts,
  sendChatMessage,
  sendTyping,
  sendReadReceipt,
} from "../websocket/socket";
import { useAuth } from "../context/AuthContext";

let typingTimeout = null;

export default function ChatWindow({ onActivity, isGroup }) {
  const { conversationId } = useParams();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState("");
  const [typingUser, setTypingUser] = useState(null);
  const [otherReadAt, setOtherReadAt] = useState(null);
  const bottomRef = useRef(null);

  useEffect(() => {
    if (!conversationId) return;

    setMessages([]);
    setTypingUser(null);
    setOtherReadAt(null);

    getMessages(conversationId).then((res) => setMessages(res.data));

    markConversationRead(conversationId).then(() => onActivity?.());
    sendReadReceipt(conversationId);

    const unsubMessages = subscribeToConversation(conversationId, (newMessage) => {
      setMessages((prev) => [...prev, newMessage]);
      sendReadReceipt(conversationId);
      onActivity?.(); // refresh the sidebar's last-message preview / unread count
    });

    const unsubTyping = subscribeToTyping(conversationId, (event) => {
      if (event.user.email === user?.email) return;
      setTypingUser(event.typing ? event.user.fullName : null);
    });

    const unsubRead = subscribeToReadReceipts(conversationId, (event) => {
      if (event.userId === user?.id) return;
      setOtherReadAt(event.readAt);
    });

    return () => {
      unsubMessages();
      unsubTyping();
      unsubRead();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [conversationId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleDraftChange = (e) => {
    setDraft(e.target.value);
    sendTyping(conversationId, true);

    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => sendTyping(conversationId, false), 1500);
  };

  const handleSend = (e) => {
    e.preventDefault();
    if (!draft.trim()) return;
    sendChatMessage(conversationId, draft.trim());
    sendTyping(conversationId, false);
    setDraft("");
    onActivity?.();
  };

  if (!conversationId) {
    return <div className="flex-1 flex items-center justify-center text-gray-400">Select a conversation</div>;
  }

  const lastOwnMessage = [...messages].reverse().find((m) => m.sender.email === user?.email);
  const lastOwnMessageSeen =
    lastOwnMessage && otherReadAt && new Date(lastOwnMessage.sentAt) <= new Date(otherReadAt);

  return (
    <div className="flex-1 flex flex-col">
      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {messages.map((m) => {
          const isMine = m.sender.email === user?.email;
          return (
            <div key={m.id} className={`flex flex-col ${isMine ? "items-end" : "items-start"}`}>
              {isGroup && !isMine && (
                <span className="text-xs text-gray-500 mb-0.5 px-1">{m.sender.fullName}</span>
              )}
              <div className={`max-w-xs px-3 py-2 rounded-lg ${isMine ? "bg-blue-600 text-white" : "bg-gray-200"}`}>
                <p className="text-sm">{m.content}</p>
              </div>
            </div>
          );
        })}
        {lastOwnMessageSeen && (
          <p className="text-xs text-gray-400 text-right pr-1">Seen</p>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="px-4 h-5 text-xs text-gray-500 italic">
        {typingUser ? `${typingUser} is typing...` : ""}
      </div>

      <form onSubmit={handleSend} className="border-t p-3 flex gap-2">
        <input
          value={draft}
          onChange={handleDraftChange}
          placeholder="Type a message..."
          className="flex-1 border rounded-lg px-3 py-2"
        />
        <button className="bg-blue-600 text-white px-4 py-2 rounded-lg">Send</button>
      </form>
    </div>
  );
}
