# Gestor de Produção

Aplicativo Android para gerenciar a produção de itens (artesanato) de Warspear Online: cadastro de personagens, profissões e trabalhos, fila de produção, estoque, vendas e uma automação opcional que inicia as produções no jogo.

- **Pacote:** `com.kevin.gestorproducao`
- **Versão atual:** 1.3.0 (`versionCode` 4)
- **SDK:** `minSdk` 24 · `targetSdk` 34 · `compileSdk` 36
- **Linguagem:** Java 8, ViewBinding, Navigation Component (Safe Args)

## Funcionalidades

- **Autenticação** com Firebase Auth: cadastro, login e recuperação de senha.
- **Personagens:** cadastro, edição e seleção do personagem ativo; cada um tem suas profissões, nível e XP (gráfico com MPAndroidChart).
- **Profissões e trabalhos:** catálogo de profissões/trabalhos, com recursos necessários e licenças de produção.
- **Produção:** fila de trabalhos por personagem, com planejamento, consumo de materiais e atualização automática do estoque (deslize o item para avançar o estado).
- **Estoque:** controle de itens produzidos e quantidades.
- **Vendas:** registro de vendas, filtros por período (semana, mês, personalizado), análise por trabalho e listagem de **chance de vendas** (nível do trabalho, marcação de itens ofertados).
- **Automação (opcional):** serviço de acessibilidade que captura a tela, lê o texto com OCR (ML Kit), detecta o estado do jogo e navega até iniciar cada produção pendente de personagens com `autoProducao` ativo.

## Arquitetura

Camadas dentro de `app/src/main/java/com/kevin/gestorproducao/`:

| Pacote | Responsabilidade |
| --- | --- |
| `ui/` | Activity única (`MainActivity`), fragments, adapters de RecyclerView, ViewModels e componentes visuais |
| `service/` | Regras de negócio: planejamento, consumo de materiais, preços, análise de vendas, fluxo de produção |
| `repository/` | Acesso aos dados no Firebase (Realtime Database) e `Resource`/`Evento` para estados de UI |
| `dao/`, `db/` | Persistência local em SQLite (`DbHelper` e contratos de tabela) |
| `model/` | Entidades (Personagem, Profissao, Trabalho, TrabalhoProducao, TrabalhoEstoque, TrabalhoVendido, ...) |
| `rules/` | Catálogos fixos (recursos, recursos raros, licenças de produção) e exceções de domínio |
| `automacao/` | Automação: orquestrador, serviço de acessibilidade, captura de tela, OCR e motor de navegação (`DetectorEstadoTela`, `NavegadorJogo`) |

Padrão geral: Fragment → ViewModel → Repository/Service → Firebase/SQLite, com `LiveData` para observação.

## Automação

A automação usa três permissões/serviços que o usuário precisa conceder manualmente:

1. **Serviço de acessibilidade** (`AutomacaoAccessibilityService`) para realizar toques.
2. **Captura de tela** (MediaProjection, via serviço em primeiro plano) para obter imagens do jogo.
3. **Notificações** para o serviço em primeiro plano.

Fluxo: captura → OCR → `DetectorEstadoTela` identifica a tela → `NavegadorJogo` decide o próximo gesto → ao confirmar visualmente que o jogo aceitou a produção, o estado é gravado pelo mesmo caminho do deslize manual.

A automação **nunca digita nada**: ao encontrar a tela de login, pede intervenção manual e não guarda a senha do jogo.

## Senha dos personagens

A senha da conta do jogo é gravada em `Personagens/<id>/senha` no formato `enc:v1:` + base64 URL-safe (com `=`) de `nonce(12) + texto cifrado + etiqueta(16)`, AES-256-GCM sem AAD, o mesmo lido pela automação WarspearVision. Valor sem o prefixo é senha crua (dados antigos) e continua válido; não há migração em massa.

A chave (32 bytes aleatórios em base64 URL-safe) nunca vai para o Firebase, o git ou o código. Em cada aparelho ela é colada uma vez no diálogo "Chave de senhas", que abre sozinho ao salvar ou abrir um personagem sem a chave (ou com chave incorreta), e fica guardada cifrada pelo Android Keystore. Um aparelho restaurado ou com o Keystore perdido pede a chave de novo.

## Configuração

### Requisitos

- Android Studio (versão recente) e JDK 17
- Um projeto Firebase com **Authentication** (e-mail/senha) e **Realtime Database**

### Passos

1. Clone o repositório e abra no Android Studio.
2. Substitua `app/google-services.json` pelo arquivo do seu projeto Firebase (o pacote deve ser `com.kevin.gestorproducao`).
3. Publique as regras do banco (`database.rules.json`) com o Firebase CLI:
   ```bash
   firebase deploy --only database
   ```
4. Execute no dispositivo/emulador (Run ▶) ou pelo Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

### Banco de dados (Realtime Database)

As regras em [`database.rules.json`](database.rules.json) exigem usuário autenticado. Nós principais: `Usuarios2/{uid}` (acesso apenas do dono), `Personagens/{id}` (apenas se pertencer ao usuário) e os catálogos/dados compartilhados `Profissoes`, `Lista_profissoes`, `Recursos`, `Lista_recursos`, `Lista_trabalhos`, `Estoque`, `Vendas` e `Producao` (leitura e escrita para autenticados).

## Testes

Testes unitários (JUnit 4 + Robolectric) em `app/src/test/`, cobrindo o motor de automação, `PricingService`, `DbHelper` e modelos.

```bash
./gradlew testDebugUnitTest
```

## Release

O build de release é feito pelo GitHub Actions ([`.github/workflows/release.yml`](.github/workflows/release.yml)) ao enviar uma tag `v*`. A tag deve ser igual ao `versionName` de `app/build.gradle`.

```bash
git tag -a v1.3.0 -m "Versao 1.3.0"
git push origin v1.3.0
```

O workflow gera o APK assinado e o publica na release do GitHub. São necessários os secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`. Localmente (sem `KEYSTORE_PATH`), o release não é assinado.

> `minifyEnabled` está desligado no release. Antes de ativá-lo, teste manualmente um release assinado (login, CRUD no Firebase e gráfico de XP); as regras de `proguard-rules.pro` já cobrem os modelos e o MPAndroidChart.

## Principais dependências

Firebase (Auth, Realtime Database, Firestore, Messaging), Material Components, Navigation, Lifecycle, ML Kit Text Recognition, MPAndroidChart, ShimmerLayout.

## Contribuição

O desenvolvimento acontece na branch `desenvolvimento`, com merge em `master` via pull request. Mensagens de commit seguem o padrão `feat:`, `fix:`, `chore:`.
