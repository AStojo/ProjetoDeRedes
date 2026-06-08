# Plano de Testes
## Sistema Distribuído de Comunicação e Partilha de Ficheiros em Java

---

| Nº |                   Teste                     |           Input         |              Resultado Esperado                |                             Resultado Obtido                           | SUCESSO? |
|----|---------------------------------------------|-------------------------|------------------------------------------------|------------------------------------------------------------------------|----------|
| 1  | Cliente liga ao servidor                    |            __           | Servidor aceita ligação e cria sessão          | Servidor aceita a ligação e cria sessão                                |    SIM   |
| 2  | Cliente usa WHO antes de NICK               |WHO                      | 401 faca login primeiro                        | 401 faça login primeiro                                                |    SIM   |
| 3  | Cliente regista nome válido                 |NICK alice               | 200 bem-vindo alice                            | 200 bem-vindo alice                                                    |    SIM   |
| 4  | Dois clientes tentam usar o mesmo nome      |NICK alice (other client)| 409 nome ja utilizado                          | 409 nome ja utilizado                                                  |    SIM   |
| 5  | Nome com menos de 3 caracteres              |NICK ab                  | 400 nome deve ter entre 3 a 20 caracteres      | 400 nome deve ter entre 3 a 20 caracteres                              |    SIM   |
| 6  | Nome com mais de 20 caracteres              |NICK abcdefghijklmnopqrst| 400 nome deve ter entre 3 a 20 caracteres      | 400 nome deve ter entre 3 a 20 caracteres                              |    SIM   |
| 7  | o nome não pode estar vazio                 |NICK                     | 400 nome invalido                              | 400 nome deve ter entre 3 a 20 caracteres                              |    SIM   |
| 8  | Cliente envia MSG                           |MSG ola a todos          | Outros clientes recebem MSG alice: ola a todos | MSG alice: ola a todos                                                 |    SIM   |
| 9  | MSG não é enviada ao próprio                |MSG ola                  | Remetente recebe 200 mensagem enviada          | 200 mensagem enviada                                                   |    SIM   |
| 10 | Cliente envia PM para utilizador existente  |PM bob ola               | bob recebe PM alice: ola                       | bob recebe PM alice: ola                                               |    SIM   |
| 11 | Cliente envia PM para utilizador inexistente|PM carlos ola            | 404 utilizador nao encontrado                  | 404 utilizador nao encontrado. Usa WHO para ver os utilizadores ligados|    SIM   |
| 12 | Cliente envia PM sem texto                  |PM bob                   | 400 formato invalido                           | 400 formato invalido.                                                  |    SIM   |
| 13 | Cliente envia comando inválido              |FOO                      | 400 comando invalido                           | 400 comando invalido. Escreve HELP para ver os comandos                |    SIM   |
| 14 | Cliente envia ficheiro válido               |SEND teste.txt           | 200 ficheiro recebido teste.txt X bytes        | 200 ficheiro recebido run.bat 224 bytes                                ||
| 15 | Ficheiro guardado em uploads/               |SEND teste.txt           | ficheiro aparece em uploads/                   | || 
| 16 | Cliente envia ficheiro acima do limite      |SEND ficheiro_grande.zip | 400 tamanho excede limite                      | ||
| 17 | Cliente tenta path traversal                |SEND ../../x.txt         | 400 nome de ficheiro invalido                  | ||
| 18 | Cliente envia ficheiro vazio                |SEND vazio.txt           | 400 ficheiro vazio                             | ||
| 19 | Cliente muda o nome após login              |NICK novo_nome           | 200 nome alterado para novo_nome               | 200 nome alterado para novo_nome                                       ||
| 20 | Outros clientes veem mudança de nome        |NICK novo_nome           | *** alice mudou o nome para novo_nome          | *** alice mudou o nome para novo_nome                                  ||
| 21 | PING ao servidor                            |PING                     | 200 PONG                                       | 200 PONG                                                               |    SIM   |
| 22 | TIME ao servidor                            |TIME                     | 200 hora do servidor: yyyy-MM-dd HH:mm:ss      | 200 hora do servidor: yyyy-MM-dd HH:mm:ss                              |    SIM   |
| 23 | WHO com vários clientes                     |WHO                      | 200 utilizadores ligados (N): alice, bob       | 200 utilizadores ligados (2): alice, bob                               |    SIM   |
| 24 | HELP antes do login                         |HELP                     | lista de comandos disponíveis                  | lista de comandos disponíveis: NICK, HELP E QUIT                       |    SIM   |
| 25 | HELP após login                             |HELP                     | lista completa de comandos e códigos           | lista completa de comandos e códigos resposta                          |    SIM   |
| 26 | QUIT limpo                                  |QUIT                     | 200 adeus — servidor remove cliente            | 200 adeus — servidor remove cliente                                    |    SIM   |
| 27 | Cliente fica inativo 3 minutos              |            —            | 408 timeout                                    | 408 timeout                                                            |    SIM   |
| 28 | 2 ou mais clientes comunicam em simultâneo  | MSG de dois clientes    | Servidor continua funcional                    | Servidor continua funcional                                            |    SIM   |
| 29 | UDP Discover com servidor ativo             | DISCOVER                | Servidor encontrado: SERVER 192.168.x.x 5000   | Servidor encontrado: SERVER 192.168.x.x 5000                           |    SIM   |----
| 30 | UDP Discover sem servidor                   | DISCOVER (sem servidor) | Nenhum servidor encontrado na rede             | Nenhum servidor encontrado na rede                                     ||                        
| 31 | Cliente fecha inesperadamente               | fechar janela           | Servidor remove cliente da lista               | Servidor remove cliente da lista                                       || |