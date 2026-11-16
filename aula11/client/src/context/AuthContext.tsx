import { createContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type {
  AuthenticatedUser,
  AuthenticationResponse,
} from "@/commons/types";
import { api } from "@/lib/axios";
import { useNavigate } from "react-router-dom";

interface AuthContextType {
  authenticated: boolean;
  authenticatedUser?: AuthenticatedUser;
  handleLogin: (authenticationResponse: AuthenticationResponse) => Promise<any>;
  handleLogout: () => void;
  hasPermission: (permission: string) => boolean;
  handleLoginSocial: (idToken: string) => void;
}

interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext({} as AuthContextType);

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [authenticated, setAuthenticated] = useState(false);
  const [authenticatedUser, setAuthenticatedUser] =
    useState<AuthenticatedUser>();
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    const storedToken = localStorage.getItem("token");

    if (storedUser && storedToken) {
      setAuthenticatedUser(JSON.parse(storedUser));
      setAuthenticated(true);
      api.defaults.headers.common["Authorization"] = `Bearer ${JSON.parse(
        storedToken
      )}`;
      navigate("/");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLogin = async (
    authenticationResponse: AuthenticationResponse
  ) => {
    try {
      localStorage.setItem(
        "token",
        JSON.stringify(authenticationResponse.token)
      );
      localStorage.setItem("user", JSON.stringify(authenticationResponse.user));
      api.defaults.headers.common[
        "Authorization"
      ] = `Bearer ${authenticationResponse.token}`;

      setAuthenticatedUser(authenticationResponse.user);
      setAuthenticated(true);
    } catch {
      setAuthenticatedUser(undefined);
      setAuthenticated(false);
    }
  };

  const handleLogout = async () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    api.defaults.headers.common["Authorization"] = "";

    setAuthenticated(false);
    setAuthenticatedUser(undefined);
  };

  const hasPermission = (permission: string): boolean => {
    if (!authenticatedUser?.authorities) {
      return false;
    }
    return authenticatedUser?.authorities.some(
              (authority) => authority.authority === permission
            );
  }

  const handleLoginSocial = async(idToken: string) => {    
    api.defaults.headers.common["Auth-Id-Token"] = `Bearer ${idToken}`;
    const response = await api.post("/auth-social");
    console.log(response);
    api.defaults.headers.common["Auth-Id-Token"] = "";
    localStorage.setItem("token", JSON.stringify(response.data.token));
    localStorage.setItem("user", JSON.stringify(response.data.user));
    api.defaults.headers.common["Authorization"] = `Bearer ${response.data.token}`;
    setAuthenticatedUser(response.data.user);
    setAuthenticated(true);
    navigate("/");
  }

  return (
    <AuthContext.Provider
      value={{
        authenticated,
        authenticatedUser,
        handleLogin,
        handleLogout,
        hasPermission,
        handleLoginSocial,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext };
