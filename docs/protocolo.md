#Protocolo de Comunicação
#Sistema Distribuído de Comunicação e Partilha de Ficheiros em Java

---

## Portas utilizadas

| Protocolo | Porta | Uso |
|-----------|-------|-----|
| TCP | 5000 | Comunicação principal entre cliente e servidor |
| UDP | 6000 | Descoberta automática do servidor na rede local |

---

## Fase de ligação

Quando um cliente se liga ao servidor via TCP, antes de poder usar o sistema
deve registar um nome com o comando NICK.

Antes do registo, apenas são aceites os comandos:
- NICK
- HELP
- QUIT

Qualquer outro comando antes do registo recebe: `401 faca login primeiro`

---

## Comandos disponíveis

### NICK
Regista o utilizador no sistema.

```
NICK alice
```

Respostas:
```
200 bem-vindo alice
400 nome invalido. Exemplo: NICK alice
400 nome deve ter entre 3 a 20 caracteres. Exemplo: NICK alice
409 nome ja utilizado. Exemplo: NICK outro_nome
```

Regras:
- nome não pode estar vazio
- nome deve ter entre 3 e 20 caracteres
- não pode haver dois utilizadores com o mesmo nome

---

### HELP
Mostra a lista de comandos disponíveis.

```
HELP
```

---

### WHO
Lista os utilizadores ligados no momento.

```
WHO
```

Resposta:
```
200 utilizadores ligados (2): alice, bob
```

---

### MSG
Envia uma mensagem pública para todos os utilizadores ligados, exceto o remetente.

```
MSG ola a todos
```

Respostas ao remetente:
```
200 mensagem enviada
400 formato invalido. Exemplo: MSG ola a todos
```

Os outros clientes recebem:
```
MSG alice: ola a todos
```

---

### PM
Envia uma mensagem privada para um utilizador específico.

```
PM bob ola
```

Respostas ao remetente:
```
200 mensagem privada enviada para bob
400 formato invalido. Exemplo: PM alice ola
404 utilizador nao encontrado. Usa WHO para ver os utilizadores ligados
```

O destinatário recebe:
```
PM alice: ola
```

---

### SEND
Envia um ficheiro para o servidor. O ficheiro é guardado na pasta uploads/.

```
SEND teste.txt
```

Respostas:
```
200 ficheiro recebido teste.txt 1024 bytes
400 nome de ficheiro invalido. Exemplo: SEND foto.jpg
400 ficheiro vazio. O ficheiro nao pode ter 0 bytes
400 tamanho excede limite. Maximo permitido: 5 MB
400 erro na transferencia
```

Protocolo binário utilizado:
```
[int nameLen][nameBytes UTF-8][long fileLen][fileBytes]
```

Limites:
- Tamanho máximo: 5 MB
- Path traversal bloqueado (ex: ../../ficheiro.txt é rejeitado)

---

### NICK (após login)
Muda o nome do utilizador após o login.

```
NICK novo_nome
```

Respostas:
```
200 nome alterado para novo_nome
400 formato invalido. Exemplo: NICK novo_nome
400 nome deve ter entre 3 a 20 caracteres. Exemplo: NICK alice
409 nome ja utilizado. Escolhe outro, exemplo: NICK outro_nome
```

Todos os outros utilizadores recebem:
```
*** alice mudou o nome para novo_nome
```

---

### PING
Verifica se o servidor está a responder.

```
PING
```

Resposta:
```
200 PONG
```

---

### TIME
Mostra a hora atual do servidor.

```
TIME
```

Resposta:
```
200 hora do servidor: 2026-06-07 14:32:10
```

---

### QUIT
Termina a sessão do cliente.

```
QUIT
```

Resposta:
```
200 adeus
```

---

## Descoberta UDP

O cliente envia um datagrama UDP por broadcast para descobrir o servidor na rede local.

Pedido do cliente (porta 6000):
```
DISCOVER
```

Resposta do servidor:
```
SERVER 192.168.68.53 5000
```

Se nenhum servidor responder em 3 segundos:
```
Nenhum servidor encontrado na rede.
```

---

## Códigos de resposta

| Código | Significado |
|--------|-------------|
| 200 | Operação realizada com sucesso |
| 400 | Pedido inválido |
| 401 | Cliente ainda não registado |
| 404 | Recurso ou utilizador não encontrado |
| 408 | Timeout |
| 409 | Conflito — nome já utilizado |
| 500 | Erro interno do servidor |

---

## Framing

### Comandos de texto
Cada comando é uma linha terminada por `\n`:
```
WHO\n
MSG ola\n
QUIT\n
```

### Ficheiros binários
Protocolo com comprimento explícito:
```
[int nameLen 4 bytes][nameBytes UTF-8][long fileLen 8 bytes][fileBytes]
```

---

## Codificação
Todas as mensagens de texto usam UTF-8 explícito para evitar
dependência do encoding do sistema operativo.