import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { searchUsers } from "../services/userService";
import { sendFriendRequest, getFriends } from "../services/friendService";
import UserCard from "../components/UserCard";

export default function SearchPage() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [sentIds, setSentIds] = useState(new Set());
  const [friendIds, setFriendIds] = useState(new Set());

  useEffect(() => {
    getFriends().then((res) => setFriendIds(new Set(res.data.map((f) => f.id))));
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    const res = await searchUsers(query);
    setResults(res.data);
  };

  const handleSend = async (targetUser) => {
    try {
      await sendFriendRequest(targetUser.id);
      setSentIds((prev) => new Set(prev).add(targetUser.id));
      toast.success(`Request sent to ${targetUser.fullName}`);
    } catch (err) {
      toast.error(err.response?.data?.message || "Could not send request");
    }
  };

  const labelFor = (u) => {
    if (friendIds.has(u.id)) return "Friends";
    if (sentIds.has(u.id)) return "Sent";
    return "Add friend";
  };

  return (
    <div className="max-w-md mx-auto mt-10 space-y-4">
      <form onSubmit={handleSearch} className="flex gap-2">
        <input value={query} onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by name or email" className="flex-1 border rounded-lg px-3 py-2" />
        <button className="bg-blue-600 text-white px-4 py-2 rounded-lg">Search</button>
      </form>

      {results.map((u) => (
        <UserCard
          key={u.id}
          user={u}
          actionLabel={labelFor(u)}
          disabled={friendIds.has(u.id) || sentIds.has(u.id)}
          onAction={handleSend}
        />
      ))}
    </div>
  );
}
