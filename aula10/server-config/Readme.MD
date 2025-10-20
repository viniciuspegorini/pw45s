# Configurando o ambiente de produção

Para apresentar uma simulação de ambiente de produção serão utilizadas as tecnologias e serviços:
- React: Aplicação cliente ([Nesse Repositório](https://github.com/viniciuspegorini/pw45s-client-deploy))
- Spring Framework: Aplicação servidor ([Nesse Repositório](https://github.com/viniciuspegorini/pw45s-server-deploy))
- PostgresSQL: Banco de dados
- Registro.br: Registro do domínio
- Cloudflare: Camada de segurança (ataques DDoS, Firewall, etc...)
- Digital Ocean: Serviço de hospedagem
- Docker: Ambiente de conteiner
- Traefik: Proxy reverso
- Jenkins: Ferramenta de CI/CD


## 1. Crianco a conta e o Droplet na Digital Ocean

Primeiro passo é criar uma conta [https://m.do.co/c/05bf7c48eb76](https://m.do.co/c/05bf7c48eb76).
Preencha com:
Nome
Email
Senha

Confirme o email recebido.

Será necessário adicionar um método de pagamento (cartão de crédito ou PayPal).
Complete o processo de verificação.

Criar um projeto e informar o nome e descrição.
Então podemos criar um droplet:
Escolha o plano de Droplet
Basic → ideal para pequenos projetos.
Modelos:
Regular with SSD → preço mais acessível.
Premium AMD/Intel → mais desempenho.
Premium NVMe → muito rápido.
Selecione o tamanho:
2 GB / 1vCPU → ideal para testes.
4 GB ou mais → para produção, lembrando que as aplicações serão diretamente impactadas de acordo com as configurações do droplet.

Agora podemos selecionar o método de autenticação. Prefira SSH Key

@R0ck5mith

No droplet criado iremos receber um IPV4, ele será utilizado na Cloudflare para redirecionamento do domínio no passo 3.5.

## 2. Configurando o domínio

Acessar o [registro.br](https://registro.br/busca-dominio/), buscar o domínio, se ele estiver livre, basta seguir o fluxo do site para realizar a compra do mesmo. Após a compra você irá receber um email para pagamento do domínio. Depois de pago o domínio estará disponível na conta para ajuste do servidor DNS.

## 3. Configurando a Cloudflare

Para adicionar uma camada de segurança já pronta, podemos adicionar os serviços fornecido pela Clouflare como proxy. 

### 3.1. Crie uma conta no Cloudflare
Acesse: https://dash.cloudflare.com/sign-up
Informe email e senha.
Confirme a conta via email.

### 3.2. Adicione um domínio
Após logar, clique em "Add a Site".
Digite o seu domínio (ex.: meudominmio.com.br).
Clique em "Add site".

### 3.3. Escolha um plano
Para maioria dos casos, o plano Free já atende.
Selecione o plano e continue.

### 3.4. Cloudflare vai escanear seu DNS atual
Ele listará todos os registros DNS atuais (A, CNAME, MX, etc.).
Revise cuidadosamente.
Se já está usando DigitalOcean, mantenha o registro A apontando para o IP público da sua Droplet, criado no final do passo Configurando a Digital Ocean. 
Vamos precisar criar subdomínios para o Jenkins, Minio, Minio Console, PgAdmin e Traefik.
Para isso criar um Registro do Tipo A apontando para o IP do Droplet na Digital Ocean.

### 3.5. Alterar os Nameservers no seu provedor de domínio
O Cloudflare vai mostrar dois Nameservers (exemplo: alice.ns.cloudflare.com e bob.ns.cloudflare.com).

Acesse o painel onde comprou o domínio (Registro.br, Namecheap, etc.).

Localize a opção de "DNS" ou "Nameservers".

Substitua pelos do Cloudflare.

⚠️ Atenção: A propagação dos Nameservers pode levar de 30 minutos até 48 horas. Geralmente leva entre 1 e 2 horas.

### 3.6. Verificação pelo Cloudflare
Após a troca, volte ao painel Cloudflare.
Clique em "Done, check nameservers".


## 4. Configurando os serviços e aplicações na Digital Ocean

### 4.1 Instalando o docker

Ajustando o firewall para expor as portas 80 e 443

```cmd
sudo ufw allow 80
sudo ufw allow 443
```

Instalando o docker:
```cmd
sudo apt update

sudo apt upgrade

# Add Docker's official GPG key:
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

```

### 4.2 Criando a rede `web`

Será necessário criar uma rede externa no docker para as aplicações.

```cmd
sudo docker network create --driver=bridge --attachable web
```

### 4.3 Configurações iniciais do Traefik

Instalar o apache2-utils no Ubuntu:

```cmd
sudo apt-get install apache2-utils
```

Gerar a senha:
```cmd
htpasswd -nbB <username> <password>
```
Ex.:
```cmd
htpasswd -nbB admin mypassword
```

Substituir a senha no arquivo `docker-compose.yml` ou no arquivo .env, lembrando de adicionar um $ a mais em cada um dos $ gerado na senha criptografada.

*** IMPORTANTE:
Aqui o próximo passo poderia ser registrar o Domínio no meu Network da Digital Ocean e ajustar os registros de DNS no local de registro (Ex. Registro.br, etc.)

Agora será necessário criar o arquivo acme.json com as permissões necessárias. O arquivo acme.json é o banco de dados local em que o Traefik armazena todos os certificados, chaves privadas e metadados emitidos pelo Let's Encrypt.

```cmd
mkdir -p ./appdata/traefik
touch ./appdata/traefik/acme.json
chmod 600 ./appdata/traefik/acme.json
chown root:root ./appdata/traefik/acme.json
```

### 4.4 Executando o docker-compose.yml

Todos os serviços estão configurados no arquivo docker-compose.yml, para executar basta criar o arquivo no servidor e executar:

```cmd
sudo docker compose up -d --build
```

### 4.5 Recuperar a senha inicial do Jenkins e configuração

Para recuperar a senha inicial do Jenkins basta executar o comando abaixo no terminal do servidor:

```cmd
sudo docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Ou listar o log do conteiner do Jenkins:

```cmd
docker logs -f jenkins
```

Após isso acesse `http://ip:8081` ou `https://jenkins.dominio.com.br`


### 5 Realizando o deploy da API

Será realizado o deploy da aplicação localizada em:
[https://github.com/viniciuspegorini/pw45s-server-deploy](https://github.com/viniciuspegorini/pw45s-server-deploy)


Será necessário criar as variáveis de ambiente com as senhas de acesso para o Postgres, Google e Minio. No menu Manage Jenkins > Credentials > System > Globla Credentias criar uma nova chave com as configurações:

##### Postgres
```properties
Kind=Username with password
Scope: Global
Username: postgres
Password: aula@PW45S
ID: postgres-id
Description: Postgres
```

##### Google
```properties
Kind=Username with password
Scope: Global
Username: admin
Password: aula@PW45S
ID: pw45s_google_client_id
Description: Google
```

##### Minio
```properties
Kind=Username with password
Scope: Global
Username: admin
Password: aula@PW45S
ID: pw45s_minio_id
Description: Minio
```
#### 5.1 Criando o pipeline

Na página principal do Jenkins clicar em `New Item` e após preencher os dados abaixo clicar em Save:

```properties
Name= Server
Type=Pipeline
Description= Server application
Definition= Pipeline script from SCM
SCM= Git
Repository URL = https://github.com/viniciuspegorini/pw45s-server-deploy
Branch Specifier: */main
```

### 6 Realizando o deploy do cliente

Será realizado o deploy da aplicação localizada em:
[https://github.com/viniciuspegorini/pw45s-client-deploy](https://github.com/viniciuspegorini/pw45s-client-deploy)

```properties
Name= Client
Type= Pipeline
Description= Client application
Definition= Pipeline script from SCM
SCM= Git
Repository URL = https://github.com/viniciuspegorini/pw45s-client-deploy
Branch Specifier: */main
```