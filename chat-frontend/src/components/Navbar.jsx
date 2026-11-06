import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  if (!user) return null;

  return (
    <nav className="bg-white border-b px-6 py-3 flex justify-between items-center">
      <div className="flex gap-4 text-sm font-medium">
        <Link to="/">Home</Link>
        <Link to="/chat">Messages</Link>
        <Link to="/search">Find people</Link>
        <Link to="/friends">Friends</Link>
        <Link to="/profile">Profile</Link>
      </div>
      <button onClick={logout} className="text-sm text-red-600">Log out</button>
    </nav>
  );
}
