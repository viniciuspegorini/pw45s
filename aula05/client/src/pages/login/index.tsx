import { useEffect, useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import { Link, useLocation, useNavigate } from "react-router-dom";
import type { AuthenticationResponse, IUserLogin } from "@/commons/types";
import { useAuth } from "@/context/hooks/use-auth";
import AuthService from "@/services/auth-service";
import { Toast } from "primereact/toast";
import { api } from "@/lib/axios";
import "./style.css";
import googleLogo from "@/assets/google-logo.png";

// URL da API que inicia a autenticação com o Google. O parâmetro redirect_uri informa para qual
// endereço do front-end a API deve redirecionar o usuário (com o token) após a autenticação.
const GOOGLE_AUTH_URL = `${api.defaults.baseURL}/oauth2/authorize/google?redirect_uri=${encodeURIComponent(
  `${window.location.origin}/login`
)}`;

export const LoginPage = () => {
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<IUserLogin>({ defaultValues: { username: "", password: "" } });
  const navigate = useNavigate();
  const { login } = AuthService;
  const toast = useRef<Toast>(null);
  const [loading, setLoading] = useState(false);

  const { handleLogin, handleLoginSocialServer } = useAuth();
  const { search } = useLocation();
  // evita processar o token duas vezes (o StrictMode executa o useEffect duas vezes em desenvolvimento)
  const socialLoginProcessed = useRef(false);

  const showGoogleError = (detail = "Falha ao efetuar autenticação com o Google.") => {
    toast.current?.show({ severity: "error", summary: "Erro", detail, life: 5000 });
  };

  //Autenticação GOOGLE - retorno da API: /login?token=... ou /login?error=...
  useEffect(() => {
    const params = new URLSearchParams(search);
    const token = params.get("token");
    const error = params.get("error");
    if ((!token && !error) || socialLoginProcessed.current) {
      return;
    }
    socialLoginProcessed.current = true;
    // remove o token/erro da URL (e do histórico do navegador)
    navigate("/login", { replace: true });

    if (error) {
      showGoogleError(error);
    } else if (token) {
      handleLoginSocialServer(token).then((success) => {
        if (!success) {
          showGoogleError();
        }
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search]);

  const onSubmit = async (userLogin: IUserLogin) => {
    setLoading(true);
    try {
      const response = await login(userLogin);
      if (response.status === 200) {
        const authenticationResponse = response.data as AuthenticationResponse;
        handleLogin(authenticationResponse);
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Login efetuado com sucesso.",
          life: 3000,
        });
        setTimeout(() => {
          navigate("/");
        }, 1000);
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Falha ao efetuar login.",
          life: 3000,
        });
      }
    } catch (error) {
      console.log(error);
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Falha ao efetuar login.",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-content-center align-items-center min-h-screen p-4">
      <Toast ref={toast} />
      <Card title="Login" className="w-full sm:w-20rem shadow-2">
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex flex-column gap-3"
        >
          <div>
            <label htmlFor="username" className="block mb-2">
              Usuário
            </label>
            <Controller
              name="username"
              control={control}
              rules={{ required: "Informe o nome de usuário" }}
              render={({ field }) => (
                <InputText
                  id="username"
                  {...field}
                  className={errors.username ? "p-invalid w-full" : "w-full"}
                />
              )}
            />
            {errors.username && (
              <small className="p-error">{errors.username.message}</small>
            )}
          </div>

          <div>
            <label htmlFor="password" className="block mb-2">
              Senha
            </label>
            <Controller
              name="password"
              control={control}
              rules={{ required: "Informe a senha" }}
              render={({ field }) => (
                <Password
                  id="password"
                  {...field}
                  toggleMask
                  feedback={false}
                  className={errors.password ? "p-invalid w-full" : "w-full"}
                  inputClassName="w-full"
                />
              )}
            />
            {errors.password && (
              <small className="p-error">{errors.password.message}</small>
            )}
          </div>

          <Button
            type="submit"
            label="Entrar"
            icon="pi pi-sign-in"
            className="w-full"
            loading={loading || isSubmitting}
            disabled={loading || isSubmitting}
          />
          <a
            className="p-button p-button-outlined p-button-secondary w-full social-btn"
            href={GOOGLE_AUTH_URL}
          >
            <img src={googleLogo} alt="Google" /> Entrar com o Google
          </a>
        </form>

        <div className="text-center mt-3">
          <small>
            Não tem uma conta?{" "}
            <Link to="/register" className="text-primary">
              Criar conta
            </Link>
          </small>
        </div>
      </Card>
    </div>
  );
};
