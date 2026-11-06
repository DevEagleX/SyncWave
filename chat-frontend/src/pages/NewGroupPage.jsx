import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { getFriends } from "../services/friendService";
import { createGroupConversation } from "../services/conversationService";

export default function NewGroupPage() {
  const [friends, setFriends] = useState([]);
  const [selected, setSelected] = useState(new Set());
  const [name, setName] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    getFriends().then((res) => setFriends(res.data));
  }, []);

  const toggle = (id) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const handleCreate = async () => {
    if (!name.trim() || selected.size === 0) {
      toast.error("Name your group and pick at least one friend");
      return;
    }
    try {
      const res = await createGroupConversation(name.trim(), Array.from(selected));
      navigate(`/chat/${res.data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.message || "Could not create group");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10 space-y-4">
      <h1 className="text-xl font-bold">New group</h1>

      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Group name"
        className="w-full border rounded-lg px-3 py-2"
      />

      <div className="space-y-2">
        <p className="text-sm font-medium text-gray-600">Add friends</p>
        {friends.length === 0 && <p className="text-sm text-gray-500">No friends yet - add some first.</p>}
        {friends.map((f) => (
          <label key={f.id} className="flex items-center gap-2 bg-white border rounded-lg p-3 cursor-pointer">
            <input type="checkbox" checked={selected.has(f.id)} onChange={() => toggle(f.id)} />
            <span>{f.fullName}</span>
          </label>
        ))}
      </div>

      <button onClick={handleCreate} className="w-full bg-blue-600 text-white py-2 rounded-lg">
        Create group
      </button>
    </div>
  );
}
