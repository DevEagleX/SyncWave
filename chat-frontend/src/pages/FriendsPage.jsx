import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import { getFriends, getIncomingRequests, acceptFriendRequest, rejectFriendRequest } from "../services/friendService";
import { startPrivateConversation } from "../services/conversationService";
import UserCard from "../components/UserCard";
import FriendRequestCard from "../components/FriendRequestCard";

export default function FriendsPage() {
  const [friends, setFriends] = useState([]);
  const [incoming, setIncoming] = useState([]);
  const navigate = useNavigate();

  const loadAll = async () => {
    const [friendsRes, incomingRes] = await Promise.all([getFriends(), getIncomingRequests()]);
    setFriends(friendsRes.data);
    setIncoming(incomingRes.data);
  };

  useEffect(() => { loadAll(); }, []);

  const handleAccept = async (id) => {
    try {
      await acceptFriendRequest(id);
      toast.success("Friend request accepted");
      loadAll();
    } catch {
      toast.error("Could not accept request");
    }
  };

  const handleReject = async (id) => {
    try {
      await rejectFriendRequest(id);
      loadAll();
    } catch {
      toast.error("Could not reject request");
    }
  };

  const handleMessage = async (friend) => {
    try {
      const res = await startPrivateConversation(friend.id);
      navigate(`/chat/${res.data.id}`);
    } catch {
      toast.error("Could not start conversation");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 space-y-8">
      <div>
        <h2 className="font-bold mb-3">Incoming requests</h2>
        <div className="space-y-2">
          {incoming.length === 0 && <p className="text-sm text-gray-500">No pending requests</p>}
          {incoming.map((r) => (
            <FriendRequestCard key={r.id} request={r} onAccept={handleAccept} onReject={handleReject} />
          ))}
        </div>
      </div>

      <div>
        <h2 className="font-bold mb-3">Your friends</h2>
        <div className="space-y-2">
          {friends.length === 0 && <p className="text-sm text-gray-500">No friends yet</p>}
          {friends.map((f) => (
            <UserCard key={f.id} user={f} actionLabel="Message" onAction={handleMessage} />
          ))}
        </div>
      </div>
    </div>
  );
}
