import { usePresence } from "../context/PresenceContext";

export default function UserCard({ user, actionLabel, onAction, disabled }) {
  const { isOnline } = usePresence();
  const online = isOnline(user.id);

  return (
    <div className="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm border">
      <div>
        <p className="font-medium flex items-center gap-2">
          {user.fullName}
          {online && <span className="w-2 h-2 bg-green-500 rounded-full" title="Online" />}
        </p>
        <p className="text-sm text-gray-500">{user.email}</p>
      </div>
      {actionLabel && (
        <button
          onClick={() => onAction(user)}
          disabled={disabled}
          className="text-sm bg-blue-600 text-white px-3 py-1 rounded-lg disabled:opacity-50"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
}
