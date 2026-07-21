-- La franja se libera tras cancelar o reprogramar; la cita conserva su
-- programación para que el historial del cliente siga mostrando fecha y hora.
ALTER TABLE public.cita
    ADD COLUMN IF NOT EXISTS fecha_programada date,
    ADD COLUMN IF NOT EXISTS hora_inicio time,
    ADD COLUMN IF NOT EXISTS hora_fin time;

UPDATE public.cita cita
SET fecha_programada = franja.fecha,
    hora_inicio = franja.hora_inicio,
    hora_fin = franja.hora_fin
FROM public.franja_horaria franja
WHERE franja.id_cita = cita.id_cita
  AND cita.fecha_programada IS NULL;

ALTER TABLE public.cita
    ALTER COLUMN fecha_programada SET NOT NULL,
    ALTER COLUMN hora_inicio SET NOT NULL,
    ALTER COLUMN hora_fin SET NOT NULL;
