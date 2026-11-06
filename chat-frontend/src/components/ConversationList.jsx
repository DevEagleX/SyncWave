import { Link, useParams } from "react-router-dom";
import { usePresence } from "../context/PresenceContext";

export default function ConversationList({ conversations }) {
  const { conversationId } = useParams();
  const { isOnline } = usePresence();

  return (
    <div className="w-72 border-r bg-white h-full overflow-y-auto">
      <Link to="/chat/new-group" className="block px-4 py-3 text-sm text-blue-600 border-b hover:bg-gray-50">
        + New group
      </Link>

      {conversations.length === 0 && (
        <p className="text-sm text-gray-500 p-4">No conversations yet. Message a friend to start one.</p>
      )}

      {conversations.map((c) => {
        const isGroup = c.type === "GROUP";
        const title = isGroup ? c.name : c.otherParticipant?.fullName;
        const online = !isGroup && c.otherParticipant && isOnline(c.otherParticipant.id);

        return (
          <Link
            key={c.id}
            to={`/chat/${c.id}`}
            className={`flex items-center justify-between px-4 py-3 border-b hover:bg-gray-50 ${
              String(c.id) === conversationId ? "bg-blue-50" : ""
            }`}
          >
            <div className="min-w-0">
              <p className="font-medium flex items-center gap-2">
                {title}
                {online && <span className="w-2 h-2 bg-green-500 rounded-full" title="Online" />}
              </p>
              <p className="text-sm text-gray-500 truncate">
                {c.lastMessage ? c.lastMessage.content : "No messages yet"}
              </p>
            </div>
            {c.unreadCount > 0 && (
              <span className="ml-2 shrink-0 bg-blue-600 text-white text-xs font-medium rounded-full px-2 py-0.5">
                {c.unreadCount}
              </span>
            )}
          </Link>
        );
      })}
    </div>
  );
}
