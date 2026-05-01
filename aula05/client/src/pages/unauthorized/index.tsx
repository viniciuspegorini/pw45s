import { Link } from "react-router-dom";

export function Unauthorized() {
  return (
    <article style={{ padding: "100px" }}>
      <h1>Oops!</h1>
      <p>Você não tem permissão para acessar o recurso solicitado!</p>
      <div className="flexGrow">
        <Link to="/">Home</Link>
      </div>
    </article>
  );
}
