# projeto-lp-java-server

Servidor da API do banco CVetti, projeto LP 3 ano

## Estrutura do Projeto

Este é um projeto Java com Gradle, preparado para deployment na AWS usando Terraform.

### Estrutura de Diretórios

```
.
├── app/                    # Código fonte da aplicação
│   ├── src/
│   │   ├── main/java/     # Código principal
│   │   └── test/java/     # Testes
│   └── build.gradle       # Configuração do módulo app
├── terraform/             # Configuração de infraestrutura AWS
│   ├── main.tf           # Provider e configuração principal
│   ├── variables.tf      # Variáveis
│   ├── outputs.tf        # Outputs
│   ├── vpc.tf            # Configuração de VPC
│   ├── compute.tf        # Load Balancer e Security Groups
│   └── README.md         # Documentação do Terraform
├── gradle/               # Gradle wrapper
├── gradlew              # Gradle wrapper script (Unix)
├── gradlew.bat          # Gradle wrapper script (Windows)
└── settings.gradle      # Configuração do projeto Gradle
```

## Requisitos

- Java 17 ou superior
- Gradle 8.5+ (incluído via wrapper)
- Para deploy: Terraform >= 1.0 e AWS CLI

## Desenvolvimento

### Build do Projeto

```bash
./gradlew build
```

### Executar a Aplicação

```bash
./gradlew run
```

### Executar Testes

```bash
./gradlew test
```

### Limpar Build

```bash
./gradlew clean
```

## Deploy na AWS

A infraestrutura AWS é gerenciada pelo Terraform. Consulte [terraform/README.md](terraform/README.md) para instruções detalhadas de deployment.

### Passos Básicos

1. Configure as credenciais AWS:
   ```bash
   aws configure
   ```

2. Inicialize o Terraform:
   ```bash
   cd terraform
   terraform init
   ```

3. Revise o plano:
   ```bash
   terraform plan
   ```

4. Aplique a infraestrutura:
   ```bash
   terraform apply
   ```

## Tecnologias

- **Java 17**: Linguagem de programação
- **Gradle**: Sistema de build
- **JUnit 5**: Framework de testes
- **Guava**: Biblioteca utilitária do Google
- **Terraform**: Infrastructure as Code
- **AWS**: Cloud provider (VPC, ALB, Security Groups)

## Contribuindo

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

