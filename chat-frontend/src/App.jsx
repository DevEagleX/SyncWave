import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { PresenceProvider } from "./context/PresenceContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import SearchPage from "./pages/SearchPage";
import FriendsPage from "./pages/FriendsPage";
import ChatPage from "./pages/ChatPage";
import NewGroupPage from "./pages/NewGroupPage";

function HomePage() {
  const { user } = useAuth();
  return <div className="p-8">Logged in as {user?.fullName}. Head to "Messages" to chat.</div>;
}

function Layout({ children }) {
  return (
    <>
      <Navbar />
      {children}
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <PresenceProvider>
        <BrowserRouter>
          <Toaster position="top-right" />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<ProtectedRoute><Layout><HomePage /></Layout></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Layout><ProfilePage /></Layout></ProtectedRoute>} />
            <Route path="/search" element={<ProtectedRoute><Layout><SearchPage /></Layout></ProtectedRoute>} />
            <Route path="/friends" element={<ProtectedRoute><Layout><FriendsPage /></Layout></ProtectedRoute>} />
            <Route path="/chat" element={<ProtectedRoute><Layout><ChatPage /></Layout></ProtectedRoute>} />
            <Route path="/chat/new-group" element={<ProtectedRoute><Layout><NewGroupPage /></Layout></ProtectedRoute>} />
            <Route path="/chat/:conversationId" element={<ProtectedRoute><Layout><ChatPage /></Layout></ProtectedRoute>} />
          </Routes>
        </BrowserRouter>
      </PresenceProvider>
    </AuthProvider>
  );
}
