<p align="center">
  <img src="assets/banner.svg" alt="dBounty Banner" width="100%" />
</p>

<p align="center">
  <a href="https://github.com/diogocorreadcs/dBounty" target="_blank">
    <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Badge"/>
  </a>
  <a href="https://getbukkit.org/download/spigot" target="_blank">
    <img src="https://img.shields.io/badge/Spigot_1.8.8-181717?style=for-the-badge&logo=minecraft&logoColor=white" alt="Spigot 1.8.8"/>
  </a>
  <a href="https://www.java.com/" target="_blank">
    <img src="https://img.shields.io/badge/Java_8+-181717?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  </a>
  <a href="LICENSE" target="_blank">
    <img src="https://img.shields.io/badge/License-MIT-181717?style=for-the-badge" alt="License"/>
  </a>
</p>

---

### Sobre o Projeto

O **dBounty** é um sistema de recompensas por cabeça modular, assíncrono e de código aberto para servidores de Minecraft 1.8.8. Desenvolvido com foco em performance, segurança anti-farm e organização estrutural limpa (MVC), o plugin utiliza **HikariCP** para persistência em **SQLite** ou **MySQL**, menus gráficos paginados com cabeças dinâmicas e integração nativa com as principais ferramentas do ecossistema.

---

### Funcionalidades

<table align="center" width="100%">
  <tr>
    <td align="center" width="33%" valign="top">
      <b>Recompensas & GUIs</b><br><br>
      • Acúmulo dinâmico de prêmios<br>
      • Menus paginados com <code>SkullMeta</code><br>
      • Drop customizado da cabeça da vítima
    </td>
    <td align="center" width="33%" valign="top">
      <b>Proteção Anti-Farm</b><br><br>
      • Bloqueio de auto-recompensa e mesmo IP<br>
      • CombatTag contra fuga em PvP (Alt+F4)<br>
      • Bloqueio por região (WorldGuard) e clã
    </td>
    <td align="center" width="33%" valign="top">
      <b>Banco de Dados & Integrações</b><br><br>
      • SQLite e MySQL assíncronos (HikariCP)<br>
      • Suporte a Vault, PlayerPoints e dClanSystem<br>
      • Webhook Discord e PlaceholderAPI
    </td>
  </tr>
</table>

---

### Comandos & Permissões

<table align="center" width="100%">
  <tr>
    <td align="left"><b>Comando</b></td>
    <td align="left"><b>Descrição</b></td>
    <td align="left"><b>Permissão Padrão</b></td>
  </tr>
  <tr>
    <td><code>/bounty</code> (<code>/recompensa</code>)</td>
    <td>Abre o menu gráfico com os jogadores procurados</td>
    <td><code>dbounty.use</code> (Geral)</td>
  </tr>
  <tr>
    <td><code>/bounty colocar &lt;jogador&gt; &lt;valor&gt;</code></td>
    <td>Coloca ou acumula uma recompensa em um jogador</td>
    <td><code>dbounty.place</code></td>
  </tr>
  <tr>
    <td><code>/bounty ver &lt;jogador&gt;</code></td>
    <td>Consulta o valor atual da cabeça de um jogador</td>
    <td><code>dbounty.check</code></td>
  </tr>
  <tr>
    <td><code>/bounty top</code></td>
    <td>Abre o ranking dos maiores caçadores de recompensas</td>
    <td><code>dbounty.top</code></td>
  </tr>
  <tr>
    <td><code>/bounty remover &lt;jogador&gt;</code></td>
    <td>Remove administrativamente a recompensa de um jogador</td>
    <td><code>dbounty.admin</code></td>
  </tr>
  <tr>
    <td><code>/bounty reload</code></td>
    <td>Recarrega os arquivos <code>config.yml</code> e <code>messages.yml</code></td>
    <td><code>dbounty.admin</code></td>
  </tr>
</table>

---

### Placeholders (PlaceholderAPI)

<table align="center" width="100%">
  <tr>
    <td align="left"><b>Placeholder</b></td>
    <td align="left"><b>Retorno</b></td>
  </tr>
  <tr>
    <td><code>%dbounty_amount%</code></td>
    <td>Valor formatado da recompensa ativa na cabeça do jogador</td>
  </tr>
  <tr>
    <td><code>%dbounty_amount_raw%</code></td>
    <td>Valor numérico bruto da recompensa ativa do jogador</td>
  </tr>
  <tr>
    <td><code>%dbounty_has_bounty%</code></td>
    <td>Retorna <code>true</code> ou <code>false</code> se o jogador estiver procurado</td>
  </tr>
  <tr>
    <td><code>%dbounty_contributors%</code></td>
    <td>Quantidade de jogadores que contribuíram para a recompensa</td>
  </tr>
  <tr>
    <td><code>%dbounty_claimed%</code></td>
    <td>Total de cabeças resgatadas pelo caçador</td>
  </tr>
  <tr>
    <td><code>%dbounty_earned%</code></td>
    <td>Total formatado de dinheiro/cash faturado caçando</td>
  </tr>
</table>

---

### Arquitetura

O projeto segue rigorosamente os padrões de separação de responsabilidades (MVC):

```text
com.diogocorreadcs.bounty/
│
├── commands/         # Executores e TabCompleter (/bounty)
├── database/         # Conexão HikariCP e repositórios SQLite/MySQL
├── integrations/     # Hooks externos (Vault, dClanSystem, WorldGuard, Discord, PAPI)
├── listeners/        # Ouvintes de abate PvP, CombatTag, conexão e menus
├── managers/         # Regras de negócio, cache em RAM, anti-farm e configurações
├── menus/            # Interfaces gráficas paginadas (GUIs)
├── models/           # Classes de modelo de dados (Bounty, HunterStats)
├── utils/            # Ferramentas auxiliares (ItemBuilder, NumberFormatter, ChatUtils)
└── BountyPlugin.java # Classe principal do plugin
```

---

### Contribuição

Este é um projeto Open Source. Sinta-se à vontade para abrir pull requests, reportar problemas ou sugerir melhorias no repositório.
