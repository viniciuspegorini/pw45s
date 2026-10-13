# 📤 Upload da imagem do produto com RustFS - Lado Cliente (React)

Nesta aula a API passou a permitir o envio de uma imagem junto com o cadastro do produto, sendo a imagem armazenada no **RustFS**, um serviço de armazenamento de objetos compatível com a API do **Amazon S3** (ver o [README.md do server](../server/README.md)). Diferente da aula anterior, em que a imagem era retornada pela API em **Base64** dentro do JSON do produto, agora a API retorna apenas o **nome** do arquivo armazenado, e o navegador carrega a imagem diretamente do RustFS.

As alterações realizadas no projeto **client** foram:

- **`src/commons/types.ts`**: novos atributos da imagem na interface `IProduct`.
- **`src/services/product-service.ts`**: função `saveAndUpload`, que envia o produto e a imagem para a API.
- **`src/pages/product-form/index.tsx`**: campo para seleção da imagem e envio do formulário com `FormData`.
- **`src/components/product-card/index.tsx`**: exibição da imagem do produto a partir do RustFS.

---

## Pré-requisitos

Para a aplicação funcionar com as imagens é necessário que estejam em execução:

1. O contêiner do **RustFS**, iniciado na pasta **server** com o comando `docker compose up -d`. A API S3 fica disponível na porta **9000** e o console web na porta **9001**.
2. A aplicação **server** (API), na porta **8080**.

Depois, basta executar o **client**:

```cmd
npm install
npm run dev
```

---

## 1. Interface IProduct

Na interface **IProduct**, em **src/commons/types.ts**, foram adicionados os atributos da imagem, retornados pela API:

```ts
export interface IProduct {
  id?: number;
  name: string;
  description: string;
  price: number;
  category: ICategory;
  imageName?: string;     // nome (key) do arquivo armazenado no bucket do RustFS
  contentType?: string;   // tipo do arquivo (ex.: image/png)
  imageFileName?: string;
  imageFile?: string;
}
```

O atributo utilizado nesta aula é o **`imageName`**: é o nome gerado pela API ao enviar o arquivo para o RustFS (um UUID com a extensão do arquivo original, ex.: `3f2a9c...e1.png`). No S3 esse nome é chamado de *key* do objeto dentro do *bucket*.

---

## 2. Enviando o produto e a imagem: multipart/form-data

Uma requisição com JSON (`application/json`) não consegue transportar um arquivo. Para enviar o produto e a imagem na mesma requisição é utilizado o formato **`multipart/form-data`**, por meio de um objeto **`FormData`**. No **ProductService** (**src/services/product-service.ts**) foi criada a função `saveAndUpload`, que envia o `FormData` para o *endpoint* `/products/upload`:

```ts
const saveAndUpload = async (formData: FormData): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post(`${productURL}/upload`, formData);
    response = {
      status: 200,
      success: true,
      message: "Produto salvo com sucesso!",
      data: data.data,
    };
  } catch (err: any) {
    response = {
      status: err.response.status,
      success: false,
      message: "Falha ao salvar produto",
      data: err.response.data,
    };
  }
  return response;
};
```

> Ao receber um `FormData`, o Axios define automaticamente o *header* `Content-Type: multipart/form-data` com o *boundary* (separador entre as partes). Por isso o *header* não deve ser informado manualmente.

Quem envia o arquivo para o RustFS é a **API**, e não o navegador. Assim, as credenciais de acesso ao RustFS (*access key* e *secret key*) ficam apenas no servidor e nunca são expostas no código do cliente.

---

## 3. Formulário de produto

No formulário de cadastro de produto (**src/pages/product-form/index.tsx**) foi adicionado um campo do tipo `file`. O arquivo selecionado é armazenado no estado `image`:

```tsx
const { findById, saveAndUpload } = ProductService;

const [image, setImage] = useState<any | null>(null);

const onFileChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
  setImage(event.target.files ? event.target.files[0] : null);
};
```

```tsx
<div>
  <label className="block mb-1">Imagem</label>
  <input
    className="form-control mb-2"
    type="file"
    name="image"
    onChange={onFileChangeHandler}
  />
  <br />
  {product?.imageName && (
    <img
      style={{ width: "100px", height: "100px" }}
      src={`http://localhost:9000/commons/${product.imageName}`}
    />
  )}
</div>
```

Na edição, a imagem atual é exibida a partir da URL do arquivo no RustFS (ver a seção 4).

Ao salvar, o formulário monta o `FormData` com duas partes: **`image`**, com o arquivo, e **`product`**, com os dados do produto em JSON. A parte `product` é criada como um `Blob` do tipo `application/json`, para que o Spring consiga convertê-la em um objeto (`@RequestPart("product") Product`). Os nomes das partes devem ser os mesmos esperados pela API:

```tsx
const onSubmit = async (data: IProduct) => {
  setLoading(true);
  try {
    const formData = new FormData();
    formData.append("image", image);
    const blob = new Blob([JSON.stringify(data)], {
      type: "application/json",
    });
    formData.append("product", blob);
    const response = await saveAndUpload(formData);
    // ...
  } finally {
    setLoading(false);
  }
};
```

---

## 4. Exibindo a imagem a partir do RustFS

Como o RustFS utiliza o formato de URL do S3 com *path-style*, a URL pública de um arquivo segue o padrão:

```
http://<endereço do RustFS>:9000/<bucket>/<key do arquivo>
```

Nesta aplicação o *bucket* é o **`commons`** e a *key* é o `imageName` retornado pela API. No componente **ProductCard** (**src/components/product-card/index.tsx**), utilizado na página `/products/show`, a imagem é exibida a partir dessa URL. Quando o produto não possui imagem, é exibida uma imagem padrão:

```tsx
<img
  alt={product.name}
  src={product?.imageName ? `http://localhost:9000/commons/${product.imageName}` : "https://primefaces.org/cdn/primereact/images/product/blue-band.jpg"}
  style={{ width: "100%", height: "200px", objectFit: "cover" }}
/>
```

Para o navegador conseguir carregar a imagem sem autenticação, o *bucket* **commons** precisa permitir a leitura pública (anônima) dos arquivos. Essa permissão é configurada pela própria API ao criar o *bucket*, por meio de uma *bucket policy* (ver a seção "Acesso público aos arquivos" no [README.md do server](../server/README.md)). Caso as imagens não carreguem e o RustFS retorne o erro **403 (Access Denied)**, verifique a política de acesso do *bucket* no console do RustFS ([http://127.0.0.1:9001/](http://127.0.0.1:9001/)).

> O endereço `http://localhost:9000` está fixo no código para simplificar o exemplo. Em uma aplicação real, o ideal é armazenar esse endereço em uma variável de ambiente do Vite (ex.: `VITE_STORAGE_URL` no arquivo `.env`), pois ele muda entre os ambientes de desenvolvimento e produção.
