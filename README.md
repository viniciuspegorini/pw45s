# Aulas da disciplina: Tópicos Avançados Em Programação Para Web - PW26S-6SI e PW45S-5SI

## API RESTful (*Back-end*)

A API REST será desenvolvida utilizando o *framework* **Spring** com a linguagem de programação Java.

### ⚙️ Lista de Ferramentas

- <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/java.png" alt="Java" width="20" /> JDK 24
- IDE:
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/intellij.png" alt="IntelliJ" title="IntelliJ"/> [IntelliJ Idea](https://www.jetbrains.com/idea/) ou 
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/eclipse.png" alt="eclipse" title="eclipse"/> [Eclipse](https://eclipseide.org/)
- SDBG:
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/postgresql.png" alt="PostgreSQL" title="PostgreSQL"/> Postgresql
- Ferramenta para testar a API:
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/postman.png" alt="Postman" title="Postman"/> Postman
    - <img width="20" src="https://icon.icepanel.io/Technology/svg/Insomnia.svg" alt="Insomnia" title="Insomnia"/> Insomnia
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/git.png" alt="Git" title="Git"/> Git
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/docker.png" alt="Docker" title="Docker"/> Docker

--- 

## Aplicação cliente (*front-end*)

O Cliente web desenvolvido utilizando a biblioteca **React** com a linguagem de programação Typescript.

### ⚙️ Lista de Ferramentas

- IDE:
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/visual_studio_code.png" alt="Visual Studio Code" title="Visual Studio Code"/> Visual Studio Code
    - <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/webstorm.png" alt="WebStorm" title="WebStorm"/> Web Storm, etc...
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/node_js.png" alt="Node.js" title="Node.js"/> Node.js
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/npm.png" alt="npm" title="npm"/> npm
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/git.png" alt="Git" title="Git"/> Git
- <img width="20" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/docker.png" alt="Docker" title="Docker"/> Docker

## Projetos:


### aula1
- Documentação da API REST com Open API 3.0 + versionamento do Banco de Dados com Flyway.

### aula2
- Admininstração das aplicações com Spring Boot Admin e Registros de Log.

### aula3
- Adição de permissões de usuário. Criação de uma classe para representar as permissões de usuário e associação da mesma na entidade de usuário.

### aula4
- Autenticação e autorização com validações das permissões no lado cliente.

### aula5
- **Autenticação utilizando o Google - lado cliente**. Criação da conta no Google Cloud Console e uso das credenciais na aplicação cliente para autenticação (retorno do idToken pelo Google) e na aplicação servidor para validação do idToken.

### aula6
- **Autenticação utilizando o Google - lado servidor**. Criação da conta no Google Cloud Console e uso das credenciais na aplicação servidor para autenticação (retorno do idToken pelo Google).

### aula7
- Upload de arquivos com armazenamento em **Banco de dados** e em Disco no **Sistema de arquivos**.

### aula8
- Upload de arquivos com armazenamento em um **sistema de armazenamento de objetos** utilizando **MINIO** (sistema de armazenamento de objetos **semelhante ao Amazon S3**).

### aula9
- Integração da API REST com APIs de processamento de Inteligência Artificial (IA).

### aula10
- Deploy de aplicações utilizando Docker.

### aula11
- Consultas na API com Spring Data JPA e Specification.


# Avaliações da disciplina:

## 1 - Seminário
### Escolha uma linguagem, plataforma e/ou framework para desenvolvimento Web (Java, PHP, Node.js, Perl, Ruby on Rails, .NET(C♯, VB), Python, etc.) ou Híbrido (Android, IOs):

- Struts2
- GWT 
- EXTJs 
- MEAN 
- Vaadin  
- *.Net 
- Django 
- Ruby on Rails 
- ZendFramework 
- YiiFramework 
- CakePHP
- Symfony   
- Grails 
- Play 
- Tapestry 
- Pyramid
- Wicket
- TurboGears
- Spark
- Ionic 
- Sails
- Bootfaces
- Meteor 
- React
- React Native
- Angular
- SolidJS
- Alpine.js
- Lit
- Node.js
- Quarkus
- Quarkus + Kotling
- Cypress
- .Net + C#
- Go
- Node.js + Nest.js
- Flask
- Svelte
- FastApi
- Laravel 
- Node.js 
- Astro  
- Vue.js 
- Nest.js
- KMM + Kotlin


1. [Deverá ser entregue] Desenvolver uma apresentação (PPT, PDF ou Readme.MD no git) contendo uma breve apresentação do *framework* e/ou biblioteca escolhida:
- As vantagens e desvantagens da linguagem, *framework* e/ou plataforma. 
- Citando as principais características. 
- Servidores Web disponíveis. 
- Configurações necessárias para rodar uma aplicação. 
- Tipo de licença de software. 
- Responsáveis pelo desenvolvimento (proprietário ou comunidade). 
- Suas conclusões sobre o uso do *framework* (facilidade para encontrar materiais, qualidade deses materiais; se é de fácil configuração, etc.)


2. [Deverá ser entregue] Deverá ser criado um tutorial de configuração do *framework* e criação de uma aplicação exemplo. 
 - A aplicação exemplo deve ser UM CRUD simples (não são necessários relacionamentos) para o cadastro de Pessoa ou qualquer outro tipo de entidade entidade (Pessoa, Livro, Produto, etc.).
 - Exemplo:
`
pessoa: {nome, cpf, telefone, rua, numero, complemento, bairro, cep, cidade, estado}
`

3. O aluno deverá apresentar o trabalho e mostrar a aplicação/código-fonte da mesma (entre 15 e 30 minutos).
	
## 2 - Projeto de desenvolvimento de software:
- Desenvolvimento do *back-end* da aplicação
- Desenvolvimento do *front-end* da aplicação
