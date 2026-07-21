-- La autenticación se delega en Supabase Auth. No se almacenan contraseñas
-- en el backend ni se utiliza la columna clave_hash del modelo global.
ALTER TABLE public.cliente
    ADD COLUMN IF NOT EXISTS auth_user_id uuid;

CREATE UNIQUE INDEX IF NOT EXISTS cliente_auth_user_id_key
    ON public.cliente (auth_user_id)
    WHERE auth_user_id IS NOT NULL;
