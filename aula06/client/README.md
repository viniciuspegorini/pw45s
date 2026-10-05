# 📤 Upload da imagem do produto - Lado Cliente (React)

Nesta aula a API passou a permitir o envio de uma imagem junto com o cadastro do produto (ver o [README.md do server](../server/README.md)). A imagem é armazenada no banco de dados e retornada pela API no formato **Base64**, dentro do próprio JSON do produto.

O projeto parte do código do cliente da **aula04**. As alterações realizadas foram:

- **`src/commons/types.ts`**: novos atributos da imagem na interface `IProduct`.
- **`src/services/product-service.ts`**: nova função `saveAndUpload`, que envia o produto e a imagem para a API.
- **`src/pages/product-form/index.tsx`**: campo para seleção da imagem e envio do formulário com `FormData`.
- **`src/components/product-card/index.tsx`**: exibição da imagem do produto.

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
  imageName?: string;     // nome do arquivo armazenado no sistema de arquivos (disco)
  imageFileName?: string; // nome do arquivo armazenado no banco de dados
  imageFile?: string;     // conteúdo da imagem armazenada no banco de dados, em Base64
}
```

---

## 2. Enviando o produto e a imagem: multipart/form-data

Uma requisição com JSON (`application/json`) não consegue transportar um arquivo. Para enviar o produto e a imagem na mesma requisição é utilizado o formato **`multipart/form-data`**, por meio de um objeto **`FormData`**. No **ProductService** (**src/services/product-service.ts**) foi criada a função `saveAndUpload`, que envia o `FormData` para o *endpoint* `/products/upload-db`:

```ts
const saveAndUpload = async (formData: FormData): Promise<IResponse> => {
  let response = {} as IResponse;
  try {
    const data = await api.post(`${productURL}/upload-db`, formData);
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

E a função é adicionada ao objeto exportado pelo *service*:

```ts
const ProductService = {
  save,
  saveAndUpload,
  findAll,
  remove,
  findById,
};
export default ProductService;
```

> Ao receber um `FormData`, o Axios define automaticamente o *header* `Content-Type: multipart/form-data` com o *boundary* (separador entre as partes). Por isso o *header* não deve ser informado manualmente. Para salvar a imagem no sistema de arquivos, basta trocar a URL para `/products/upload-fs`.

---

## 3. Formulário de produto

No formulário de cadastro de produto (**src/pages/product-form/index.tsx**) foi adicionado um campo do tipo `file`. O arquivo selecionado é armazenado no estado `image`:

```tsx
const { findById, save, saveAndUpload } = ProductService;

const [image, setImage] = useState<File | null>(null);

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
    accept="image/png, image/jpeg, image/gif, image/webp"
    onChange={onFileChangeHandler}
  />
  <br />
  {product?.imageFile && (
    <img
      style={{ width: "100px", height: "100px" }}
      src={`data:image;base64,${product.imageFile}`}
    />
  )}
</div>
```

- O atributo `accept` faz o navegador exibir apenas arquivos de imagem na janela de seleção. É apenas uma facilidade para o usuário: a validação definitiva do tipo do arquivo é feita na API.
- Na edição, a imagem atual é exibida a partir do Base64 retornado pela API, utilizando uma *data URL*: `data:image;base64,<conteúdo>`.

Ao salvar, o formulário monta o `FormData` com duas partes: **`image`**, com o arquivo, e **`product`**, com os dados do produto em JSON. A parte `product` é criada como um `Blob` do tipo `application/json`, para que o Spring consiga convertê-la em um objeto (`@RequestPart("product") ProductDTO`). Os nomes das partes devem ser os mesmos esperados pela API:

```tsx
const onSubmit = async (data: IProduct) => {
  setLoading(true);
  try {
    let response: IResponse;
    if (image) {
      // Uma nova imagem foi selecionada: envia o produto e a imagem (multipart/form-data)
      const formData = new FormData();
      formData.append("image", image);
      const blob = new Blob([JSON.stringify(data)], {
        type: "application/json",
      });
      formData.append("product", blob);
      response = await saveAndUpload(formData);
    } else {
      // Nenhuma imagem selecionada (ex.: edição apenas dos dados): salva somente o produto,
      // mantendo a imagem atual, que já está em data.imageFile
      response = await save(data);
    }
    // ... exibe a mensagem de sucesso ou de erro, igual ao formulário anterior
  } finally {
    setLoading(false);
  }
};
```

Quando nenhuma imagem é selecionada (por exemplo, ao editar apenas o nome ou o preço de um produto), o produto é salvo com a função `save`, que envia apenas o JSON. Como o JSON contém o atributo `imageFile` recebido da API, a imagem atual é mantida. Sem esse tratamento, o `FormData` seria enviado sem o arquivo e a API responderia com erro 400 (parte `image` ausente).

---

## 4. Exibindo a imagem no ProductCard

No componente **ProductCard** (**src/components/product-card/index.tsx**), utilizado na página `/products/show`, a imagem do produto é exibida a partir do Base64. Quando o produto não possui imagem, é exibida uma imagem padrão:

```tsx
<img
  alt={product.name}
  src={product?.imageFile ? `data:image;base64,${product.imageFile}` : "https://primefaces.org/cdn/primereact/images/product/blue-band.jpg"}
  style={{ width: "100%", height: "200px", objectFit: "cover" }}
/>
```
