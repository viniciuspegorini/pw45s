import { useState, useRef } from "react";
import { Toast } from "primereact/toast";
import type { IOllamaRequest } from "@/commons/types";
import { Controller, useForm } from "react-hook-form";
import { InputTextarea } from "primereact/inputtextarea";
import { Button } from "primereact/button";
import AIService from "@/services/ai-service";
import { Card } from "primereact/card";

const DEFAULT_PROMPT =
  "Responda em uma frase: o que é Spring Boot?";

export const OllamaFormPage = () => {
  const [loading, setLoading] = useState(false);
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<IOllamaRequest>({
    defaultValues: { prompt: DEFAULT_PROMPT },
  });
  const toast = useRef<Toast>(null);
  const { askOllama } = AIService;
  const [ollamaAnswer, setOllamaAnswer] = useState<string>();

  const onSubmit = async (data: IOllamaRequest) => {
    setOllamaAnswer(undefined);
    setLoading(true);
    try {
      const response = await askOllama(data.prompt);
      if (
        (response.status === 201 || response.status === 200) &&
        response.data !== undefined
      ) {
        const text =
          typeof response.data === "string"
            ? response.data
            : JSON.stringify(response.data);
        setOllamaAnswer(text);
        toast.current?.show({
          severity: "success",
          summary: "Sucesso",
          detail: "Resposta recebida do Ollama.",
          life: 3000,
        });
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erro",
          detail: "Não foi possível obter a resposta.",
          life: 3000,
        });
      }
    } catch {
      toast.current?.show({
        severity: "error",
        summary: "Erro",
        detail: "Não foi possível obter a resposta.",
        life: 3000,
      });
    } finally {
      setLoading(false);
      reset({ prompt: DEFAULT_PROMPT });
    }
  };

  return (
    <div className="container mx-auto px-4 pt-24 max-w-xl">
      <Toast ref={toast} />

      <h2 className="text-2xl mb-4">Consulta Ollama</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 p-fluid">
        <div>
          <label htmlFor="prompt" className="block mb-2">
            Prompt
          </label>
          <Controller
            name="prompt"
            control={control}
            rules={{ required: "O prompt é obrigatório" }}
            render={({ field }) => (
              <InputTextarea
                id="prompt"
                {...field}
                rows={5}
                autoResize
                className="w-full"
                placeholder="Digite a pergunta ou instrução para o modelo"
              />
            )}
          />
          {errors.prompt && (
            <small className="p-error">{errors.prompt.message}</small>
          )}
        </div>

        <div className="flex justify-end gap-2 mt-4">
          <Button
            type="submit"
            label="Enviar"
            loading={loading || isSubmitting}
            disabled={loading || isSubmitting}
          />
        </div>
      </form>
      {ollamaAnswer !== undefined && (
        <Card
          title="Resposta"
          className="w-full max-w-lg shadow-2 border-round-2xl mt-4"
        >
          <pre style={{ whiteSpace: "pre-wrap", fontFamily: "inherit" }}>
            {ollamaAnswer}
          </pre>
        </Card>
      )}
    </div>
  );
};
