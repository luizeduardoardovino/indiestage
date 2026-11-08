# Evidências técnicas — 07/11/2026

Não representam aprovação acadêmica ou domínio automático pelo estudante.

## Builds e testes

Quatro aplicações empacotadas fora da IDE por Maven Wrapper. Espaços: 7 testes, 0 falhas/erros. Eventos: 7 testes, 0 falhas/erros. Incluem serviço, CRUD HTTP, validação, paginação, contrato de dependência e reabertura de banco em arquivo. Dockerfiles construíram imagens a partir da fonte, sem depender de JAR do host.

## Cenário local de quatro processos

Script scripts/Verificar-Arquitetura.ps1: APROVADO. Eventos/Espaços registrados no Eureka, rotas por nome lógico no Gateway, detalhes completos, interrupção do processo Espaços, fallback, métrica OPEN, listagem de Eventos preservada, restauração, detalhes completos e métrica CLOSED.

Resultado local gerado: .verificacoes/runtime-f96757668fcb460da0ab279bafc9c81e/resultado.json.

## Cenário em containers

Script scripts/Verificar-Compose.ps1: APROVADO.

- Sete serviços executando: quatro aplicações e Alloy/Loki/Grafana.
- Quatro aplicações com healthcheck saudável; Gateway health UP.
- Dados normais: objeto espaco com status DISPONIVEL, obtido pela API de Espaços.
- docker compose stop espacos: detalhes com TEMPORARIAMENTE_INDISPONIVEL; registro de CallNotPermittedException com identificador da execução; listagem de Eventos preservada.
- docker compose start espacos: após saúde/chamadas de prova, detalhes DISPONIVEL, sem editar código ou reconstruir imagem.
- Loki retornou logs de Gateway, Eventos e Espaços para o mesmo correlationId: demonstracao-compose-34ad0b9cbb124a2b82d2ffda29ca593a.
- API Grafana confirmou datasource/painel provisionado; inspeção visual e prática do estudante ainda devem ocorrer.
- Consulta de labels no Loki também mostrou eureka-server, além dos três serviços do fluxo.
- Volumes de banco confirmados: indiestage_espacos-data e indiestage_eventos-data; cada um montado somente em seu serviço.

Arquivos locais gerados: .verificacoes/resultado-compose.json, logs-correlacionados.json e containers.json. Não versionar logs brutos ou segredos; os scripts permitem reproduzir os ensaios.

## Ajustes encontrados pela execução real

Separação de Builder HTTP comum e LoadBalanced em Eventos; configuração de timeout do teste alinhada à aplicação; espera por saúde após reiniciar; linhas de propriedades Docker corrigidas; descoberta contínua de arquivos no Alloy para capturar logs criados após iniciar. Todos foram incorporados à fonte/configuração antes da aprovação final.

## O que ainda cabe à entrega

Revisão de compreensão, prática de Docker/Git/GitHub, autorização para commit/push/publicação, repositório público, PDF com identificação/URL final e arguição ao vivo. Não foi realizada submissão acadêmica.