export default function FriendRequestCard({ request, onAccept, onReject }) {
  return (
    <div className="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm border">
      <div>
        <p className="font-medium">{request.sender.fullName}</p>
        <p className="text-sm text-gray-500">{request.sender.email}</p>
      </div>
      <div className="flex gap-2">
        <button onClick={() => onAccept(request.id)} className="text-sm bg-green-600 text-white px-3 py-1 rounded-lg">Accept</button>
        <button onClick={() => onReject(request.id)} className="text-sm bg-gray-300 px-3 py-1 rounded-lg">Reject</button>
      </div>
    </div>
  );
}
