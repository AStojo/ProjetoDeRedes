# Plano de Testes

| # 
| Ação 
| Input 
| Resultado Esperado 
| Resultado Obtido 
| OK? 
|
|---|------|-------|--------------------|------------------|---|
| 1 
| Ligação com nome válido 
| "alice" 
| 200 bem-vindo alice 
| 
| 
|
| 2 
| Nome já em uso 
| "alice" (2.º cliente) 
| 409 nome ja utilizado 
| 
| 
|
| 3 
| Nome curto (<3 chars) 
| "ab" 
| 400 nome deve ter entre 3 a 20 caracteres 
| 
| 
|
| 4 
| HELP 
| HELP 
| 200 comandos: WHO, MSG... 
| 
| 
|
| 5 
| WHO com 2 clientes 
| WHO 
| 200 utilizadores: alice, bob 
| 
| 
|
| 6 
| MSG público 
| MSG olá a todos 
| todos recebem a mensagem 
| 
| 
|
| 7 
| PM válido 
| PM bob segredo 
| bob recebe, alice vê confirmação 
| 
| 
|
| 8 
| PM destinatário inexistente 
| PM carlos ola 
| 404 utilizador nao encontrado 
| 
| 
|
| 9 
| SEND ficheiro válido 
| SEND foto.jpg 
| 200 ficheiro recebido 
| 
| 
|
| 10 
| SEND ficheiro >5MB 
| ficheiro 6MB 
| 400 tamanho excede limite 
| 
| 
|
| 11 
| Timeout 3 min sem input 
| — 
| 408 timeout 
| 
| 
|
| 12 
| QUIT 
| QUIT 
| 200 adeus, socket fecha 
| 
| 
|
| 13 
| Comando inválido 
| FOO 
| 400 comando invalido 
| 
| 
|
| 14 
| Descoberta UDP 
| DISCOVER 
| SERVER 192.168.x.x 5000 
| 
| 
|