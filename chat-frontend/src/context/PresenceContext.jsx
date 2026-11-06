import { createContext, useContext, useEffect, useState } from "react";
import { connectSocket, disconnectSocket, subscribeToPresence } from "../websocket/socket";
import { useAuth } from "./AuthContext";

const PresenceContext = createContext();

export function PresenceProvider({ children }) {
  const { user } = useAuth();
  const [connected, setConnected] = useState(false);
  const [presenceMap, setPresenceMap] = useState({});

  useEffect(() => {
    if (!user) {
      disconnectSocket();
      setConnected(false);
      return;
    }

    connectSocket(() => {
      setConnected(true);
      subscribeToPresence((event) => {
        setPresenceMap((prev) => ({ ...prev, [event.userId]: event.status }));
      });
    });

    return () => {
      disconnectSocket();
      setConnected(false);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const isOnline = (userId) => presenceMap[userId] === "ONLINE";

  return (
    <PresenceContext.Provider value={{ connected, isOnline }}>
      {children}
    </PresenceContext.Provider>
  );
}

export const usePresence = () => useContext(PresenceContext);
