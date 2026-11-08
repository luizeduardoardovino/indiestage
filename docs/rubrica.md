# Rubrica — verificação técnica e preparação de demonstração

07/11/2026. Referência: 20 critérios transcritos dos prints atuais. Isto não é nota nem confirmação de aprovação do professor. Implementação, testes, compreensão e demonstração em arguição são estados distintos.

| Item | Verificação e evidência | Situação |
| --- | --- | --- |
| C1.1 Aplicações independentes e APIs/status | Eventos/Espaços têm POM, Wrapper, main e bancos próprios; CRUD e status testados por HTTP | Técnico verificado |
| C1.2 Camadas | Controller, Service, Repository, entidades e DTOs próprios; cliente REST isolado em Eventos | Implementado e exercitado |
| C1.3 Dois ambientes sem segredos no código | Perfis local/docker; caminhos, descoberta e console mudam; senhas Docker no .env ignorado | Perfis local e docker executados e verificados |
| C1.4 Build fora da IDE | Quatro builds Wrapper/JAR independentes e imagens construídas da fonte | Verificado |
| C2.1 Persistência JPA | Entidades Evento/Espaco, Hibernate, Repositories e H2 | Verificado por integração |
| C2.2 Bancos exclusivos | Arquivos/volumes distintos; nenhum acesso cruzado ou associação JPA entre domínios | Implementado; execução distribuída exercitada |
| C2.3 Dados iniciais | Quatro exemplos por banco, somente se tabela vazia; reabertura sem duplicação | Testado |
| C2.4 Paginação nativa | PageRequest e JpaRepository.findAll(Pageable) nos dois serviços | Testado por HTTP e Gateway |
| C3.1 Eureka | Registros ESPACOS/EVENTOS e resolução dinâmica | Verificado em processos reais |
| C3.2 Gateway funcional | Rotas lb://espacos e lb://eventos, entrada externa | Verificado em processos reais |
| C3.3 Eventos chama Espaços por API/nome | GET /api/eventos/{id}/detalhes, RestClient LoadBalanced, bancos sem acoplamento direto | Testado com contrato e aplicações reais |
| C3.4 README | Arquitetura, rotas, configuração, builds, execução, testes, comunicação, falhas e observabilidade | Documentado; revisar URL de entrega depois |
| C4.1 Resilience4j | Circuit breaker espacos envolve chamada real, com timeouts | OPEN/CLOSED medidos no ensaio local |
| C4.2 Fallback coerente | Evento preservado; dados externos nulos e status explícito; 404 distinguido de indisponibilidade | Testado |
| C4.3 Interromper container e explicar recuperação | Roteiro e script stop/start de Espaços; observação pelo estudante e professor ainda necessárias | Ensaio técnico em containers aprovado; apresentação ao vivo pendente |
| C4.4 Desacoplamento | Contratos HTTP/DTOs, IDs sem FK entre bancos, falha não derruba CRUD de Eventos | Verificado no ensaio local |
| C5.1 Dockerfiles e Compose funcional | Quatro Dockerfiles multi-stage, sete serviços, volumes e healthchecks | Imagens, inicialização, falha e recuperação verificadas |
| C5.2 Actuator e logs centralizados/correlacionáveis | Health/métricas, X-Correlation-Id, Alloy → Loki → Grafana provisionados | Health, logs centralizados e correlação verificados |
| C5.3 Testes de diferentes camadas | 7 testes Espaços + 7 Eventos, incluindo serviço e HTTP/Repository/banco | 14 aprovados |
| C5.4 Integração efetiva | HTTP real → controller → service → banco; contrato remoto; quatro aplicações reais e falha | Testado; cenário em Compose também aprovado |

## Evidências e limites

- Logs consolidados das suítes ficam na raiz .verificacoes. build-final-integracao.log registra os testes de Eventos; build-operacao-espacos.log registra os de Espaços.
- Ensaio local completo: .verificacoes/runtime-f96757668fcb460da0ab279bafc9c81e/resultado.json.
- Ensaio Compose: .verificacoes/resultado-compose.json produzido com status APROVADO.
- Os arquivos de log, bancos e .env não devem ir para o repositório público. Os scripts permitem reproduzir a verificação.
- Arguição ao vivo, domínio do estudante, repositório público e PDF final não foram concluídos pelo teste automático.

## Limites técnicos que devem ser explicados

H2 incorporado usa uma instância por arquivo e não resolve concorrência de réplicas sobre um mesmo volume. A configuração usa ddl-auto=update para a demonstração, sem migrações versionadas. Não há validação remota de existência do espaço no cadastro; a consulta de detalhes informa NAO_ENCONTRADO quando a referência não existe. Não há reserva, transação distribuída, alta disponibilidade do catálogo nem promessa de desempenho sob carga. Esses pontos não devem ser apresentados como implementados.