import { Route, Routes } from "react-router-dom";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { HomePage } from "@/pages/home";
import { RequireAuth } from "@/components/require-auth";
import { Layout } from "@/components/layout";
import { CategoryListPage } from "@/pages/category-list";
import { CategoryFormPage } from "@/pages/category-form";
import { ProductListPage } from "@/pages/product-list";
import { ProductFormPage } from "@/pages/product-form";
import { NotFound } from "@/pages/not-found";
import { ProductShow } from "@/pages/product-show";
import { Unauthorized } from "@/pages/unauthorized";
import { MusicFormPage } from "@/pages/music-form";

const ROLES = {
  User: "ROLE_USER",
  Admin: "ROLE_ADMIN",
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* protected routes - Roles: User and Admin */}
        <Route element={<RequireAuth allowedRoles={[ROLES.Admin, ROLES.User]} />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/home" element={<HomePage />} />

          <Route path="unauthorized" element={<Unauthorized />} />

          <Route path="/categories" element={<CategoryListPage />} />
          <Route path="/categories/new" element={<CategoryFormPage />} />
          <Route path="/categories/:id" element={<CategoryFormPage />} />

          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/new" element={<ProductFormPage />} />
          <Route path="/products/:id" element={<ProductFormPage />} />

          <Route path="/music" element={<MusicFormPage />} />
          {/* catch all */}
          <Route path="*" element={<NotFound />} />
        </Route>

        {/* protected routes - Roles: Admin */}
        <Route element={<RequireAuth allowedRoles={[ROLES.Admin]} />}>
          <Route path="/products/show" element={<ProductShow />} />
        </Route>
      </Route>
    </Routes>
  );
}
