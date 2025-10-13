import { useState, useRef } from "react";
import { Toast } from "primereact/toast";
import type { IMusicRequest, IMusicResponse } from "@/commons/types";
import { Controller, useForm } from "react-hook-form";
import { InputText } from "primereact/inputtext";
import { Button } from "primereact/button";
import AIService from "@/services/ai-service";
import { Card } from "primereact/card";

export const MusicFormPage = () => {
  const [loading, setLoading] = useState(false);
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<IMusicRequest>({
    defaultValues: { theme: "", genre: "" },
  });
  const toast = useRef<Toast>(null);
  const { getMusicWithParameters } = AIService;
  const [musicResponse, setMusicResponse] = useState<IMusicResponse>();

  const onSubmit = async (data: IMusicRequest) => {
    setLoading(true);
    try {
      const response = await getMusicWithParameters(data.genre, data.theme);
      if (
        (response.status === 201 || response.status === 200) &&
        response.data
      ) {
        console.log(response.data);
        setMusicResponse(response.data as IMusicResponse);
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Música gerada com sucesso.",
          life: 3000,
        });
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Não foi possível salvar o registro.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível salvar o registro.",
        life: 3000,
      });
    } finally {
      setLoading(false);
      reset();
    }
  };

  return (
    <div className="container mx-auto px-4 pt-24 max-w-xl">
      <Toast ref={toast} />

      <h2 className="text-2xl mb-4">Gerar Música</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 p-fluid">
        <div>
          <label htmlFor="genre" className="block mb-2">
            Gênero
          </label>
          <Controller
            name="genre"
            control={control}
            rules={{ required: "O gênero musical é obrigatório" }}
            render={({ field }) => (
              <InputText
                id="genre"
                {...field}
                placeholder="Digite o gênero musical"
              />
            )}
          />
          {errors.genre && (
            <small className="p-error">{errors.genre.message}</small>
          )}
        </div>

        <div>
          <label htmlFor="theme" className="block mb-2">
            Tema
          </label>
          <Controller
            name="theme"
            control={control}
            rules={{ required: "O tema da musica é obrigatório" }}
            render={({ field }) => (
              <InputText
                id="theme"
                {...field}
                placeholder="Digite o tema da música"
              />
            )}
          />
          {errors.theme && (
            <small className="p-error">{errors.theme.message}</small>
          )}
        </div>

        <div className="flex justify-end gap-2 mt-4">
          <Button
            type="submit"
            label="Gerar"
            loading={loading || isSubmitting}
            disabled={loading || isSubmitting}
          />
        </div>
      </form>
      {musicResponse && (
        <Card
          title={musicResponse.title}
          subTitle={`${musicResponse.genre} • ${musicResponse.theme}`}
          className="w-full max-w-lg shadow-2 border-round-2xl mt-4"
        >
          <pre style={{ whiteSpace: "pre-wrap", fontFamily: "inherit" }}>
            {musicResponse.music}
          </pre>
        </Card>
      )}
    </div>
  );
};
