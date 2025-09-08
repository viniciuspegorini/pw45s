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

  /**
   * Verifica se o usuário tem uma determinada permissão
   * @param user Usuário autenticado
   * @param permission Nome da permissão a ser verificada
   * @returns true se o usuário possuir a permissão
   */
  const hasPermission = (permission: string): boolean => {
    if (!authenticatedUser?.authorities) {
      return false;
    }

    return authenticatedUser?.authorities.some(
      (auth) => auth.authority === permission
    );
  };

  return (
    <AuthContext.Provider
      value={{
        authenticated,
        authenticatedUser,
        handleLogin,
        handleLogout,
        hasPermission,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext };
