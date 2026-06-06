# Análise Comparativa de Algoritmos com Uso de Paralelismo (CPU vs GPU)

**Autores:** Amanda Lira Andrade Botelho

---

## 1. Resumo
Este trabalho apresenta uma análise detalhada do impacto do paralelismo de hardware no desempenho de algoritmos de busca textual. Utilizando a linguagem Java, foi desenvolvida uma aplicação com interface gráfica assíncrona (Swing) para contabilizar as ocorrências exatas de termos em grandes massas de dados. O ecossistema de testes compara o paradigma sequencial (`SerialCPU`), o concorrente em múltiplos núcleos (`ParallelCPU` via `ExecutorService`) e o processamento massivo em placa de vídeo (`ParallelGPU` via OpenCL/JOCL). Foram coletadas baterias de 3 amostras por cenário para mitigar distorções de inicialização de ambiente. Os resultados salvos em arquivos CSV e plotados em gráficos revelam o comportamento do *overhead* de comunicação em barramentos, a latência de inicialização da máquina virtual Java (JVM) e o fenômeno de gargalo por troca de contexto (*context switching*).

## 2. Introdução
A necessidade de processar volumes massivos de dados textuais de forma eficiente exige que desenvolvedores compreendam as características arquiteturais do hardware subjacente. Neste estudo, o problema de contagem de palavras foi submetido a três abordagens distintas:
1. **Serial CPU**: Processamento em uma única *thread*, realizando a varredura sequencial dos buffers de memória.
2. **Parallel CPU**: Divisão lógica dos arquivos de texto em fatias (*slices*) proporcionais, processadas concorrentemente por um *pool* de *threads* fixo gerenciado pela CPU.
3. **Parallel GPU**: Transferência dos dados textuais da memória RAM (Host) para a VRAM (Device) através do barramento PCI-Express, executando um Kernel escrito em C (OpenCL) de forma massivamente paralela em centenas de unidades de computação gráfica.

## 3. Metodologia
A arquitetura do sistema foi projetada de forma modular e resiliente no ecossistema Java 17 com gerenciamento de dependências via Maven.
* **Massa de Dados**: Foram utilizados três livros de domínio público com volumetrias e naturezas textuais distintas: *Don Quixote*, *Dracula* e *Moby Dick*, totalizando uma busca sobre a palavra-alvo **"whale"** em modo de busca exata.
* **Isolamento Assíncrono**: As chamadas de processamento pesado foram encapsuladas em uma `SwingWorker` para garantir que a interface visual baseada em eventos (EDT) permanecesse responsiva durante os cálculos matemáticos.
* **Estratégia Estatística**: Conforme exigido, cada teste realizou 3 execuções seguidas (*Amostras 1, 2 e 3*). A primeira rodada representa o estado "frio" da aplicação (*Cold Run*), enquanto as rodadas subsequentes refletem o estado "quente" (*Warm Run*), permitindo calcular uma média fidedigna livre de ruídos de carregamento de infraestrutura.
* **Mecanismo de Memória**: Utilizou-se `ByteBuffer` mapeado em modo de leitura compartilhada (`FileChannel.MapMode.READ_ONLY`) para garantir que o acesso aos arquivos físicos ocorresse diretamente pelo gerenciamento de paginação do sistema operacional, minimizando cópias desnecessárias na Heap do Java.

## 4. Dependências e Execução (IMPORTANTE)
O projeto utiliza o **Maven** como gerenciador de build. Para compilar e executar o software com suporte completo aos gráficos e ao motor gráfico OpenCL na máquina de avaliação, as seguintes dependências devem constar no arquivo `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.jocl</groupId>
        <artifactId>jocl</artifactId>
        <version>2.0.4</version>
    </dependency>
    
    <dependency>
        <groupId>org.jfree</groupId>
        <artifactId>jfreechart</artifactId>
        <version>1.5.4</version>
    </dependency>
</dependencies>
```
*Nota: Para o correto funcionamento automático da aplicação, os arquivos de texto (`.txt`) devem ser posicionados dentro de uma pasta chamada `samples` na raiz do diretório do projeto, ou inseridos manualmente na interface através do recurso Drag & Drop ou seleção de arquivos.*

## 5. Resultados e Discussão

Os testes práticos geraram a seguinte matriz de dados, baseada na busca exata da palavra "whale", salva automaticamente via exportador CSV do projeto:

### Tabela 1: Resultados Gerais (Consolidado de todos os livros)
| Algoritmo | Ocorrências Totais | Amostra 1 (ms) | Amostra 2 (ms) | Amostra 3 (ms) | Média (ms) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Serial CPU** | 1100 | 32,15 | 7,44 | 7,35 | **15,65** |
| **Parallel CPU (Max)** | 1100 | 112,18 | 3,88 | 4,31 | **40,12** |
| **Parallel CPU (6 Threads)** | 1100 | 4,32 | 3,53 | 4,82 | **4,22** |
| **Parallel GPU** | 1100 | 126,86 | 73,01 | 72,43 | **90,77** |

### Tabela 2: Detalhamento de Performance por Livro Isolado
| Livro | Algoritmo | Ocorrências | Amostra 1 (ms) | Amostra 2 (ms) | Amostra 3 (ms) | Média (ms) |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **DonQuixote** | Serial CPU | 0 | 3,70 | 3,51 | 4,76 | 3,99 |
| **DonQuixote** | Parallel CPU | 0 | 2,89 | 2,48 | 2,50 | 2,62 |
| **DonQuixote** | Parallel GPU | 0 | 65,61 | 67,22 | 67,76 | 66,86 |
| **Dracula** | Serial CPU | 1 | 2,27 | 2,30 | 2,36 | 2,31 |
| **Dracula** | Parallel CPU | 1 | 1,65 | 2,66 | 1,79 | 2,04 |
| **Dracula** | Parallel GPU | 1 | 71,93 | 75,75 | 72,33 | 73,33 |
| **MobyDick** | Serial CPU | 1099 | 3,11 | 3,28 | 3,08 | 3,16 |
| **MobyDick** | Parallel CPU | 1099 | 2,44 | 2,05 | 1,97 | 2,15 |
| **MobyDick** | Parallel GPU | 1099 | 73,23 | 77,64 | 68,40 | 73,09 |

### Análise Crítica dos Dados

1. **Validação e Integridade Matemática**: Todas as arquiteturas reportaram rigorosamente o mesmo número de ocorrências (1100 totais, sendo 1099 concentradas no livro *Moby Dick* devido à natureza temática da obra). Isto valida a precisão dos ponteiros, das fatias lógicas de busca e o correto tratamento de caracteres da codificação UTF-8 entre Java e C.
2. **O Fenômeno do Custo de Inicialização (Cold Run)**: Fica evidente o impacto do *overhead* inicial na Amostra 1 para as arquiteturas paralelas complexas. O método `Parallel CPU (Max)` saltou para 112,18 ms na primeira rodada antes de estabilizar em patamares excelentes de ~3,88 ms. Esse atraso deve-se ao tempo gasto pela JVM para instanciar o *pool* de *threads* e aplicar otimizações do compilador JIT (*Just-In-Time*). O mesmo ocorre na `Parallel GPU` (126,86 ms na Amostra 1), decorrente do tempo de compilação em tempo de execução do código C do Kernel e da criação do contexto OpenCL.
3. **O Paradoxo da Concorrência (Gargalo de Múltiplas Threads)**: Um dos insights mais valiosos do experimento reside na comparação entre `Parallel CPU (Max)` e `Parallel CPU (6 Threads)`. A execução com uma quantidade fixa e equilibrada de núcleos (6 Threads) obteve uma média final drasticamente menor (**4,22 ms**) do que a capacidade máxima combinada (**40,12 ms**). Isso prova empiricamente que tentar hiper-particionar um problema de tamanho moderado gera disputa por hardware, onde o processador gasta mais tempo realizando a troca de contexto (*Context Switching*) e gerenciando a sincronização das barreiras do pool do que executando o cálculo bruto de busca.
4. **O Gargalo de Latência da GPU (PCI-Express vs VRAM)**: A busca paralela na GPU apresentou o maior tempo médio consolidado (**90,77 ms**). Embora a GPU conte com centenas de núcleos de processamento idôneos para paralelismo massivo, o volume de dados de livros individuais (na casa de poucos megabytes) é pequeno demais para compensar o gargalo de transferência de dados do Host (CPU/RAM) para o Device (GPU/VRAM) através do barramento PCI-Express. A placa gráfica passa mais tempo esperando o tráfego dos bytes pelo barramento do que efetivamente executando os cálculos.

## 6. Conclusão
A elaboração deste ecossistema experimental permitiu consolidar conceitos fundamentais de sistemas concorrentes e distribuídos. Conclui-se que não existe uma arquitetura computacional absolutamente superior, mas sim escolhas de projeto adequadas à escala do problema:
* A execução **Serial** é ideal para cenários ultra-leves pela ausência completa de infraestrutura de controle (como notado na tabela individual do livro *Dracula* e *Don Quixote*, onde bateu de frente com a versão paralela).
* O paralelismo em **CPU (com controle estrito de Threads)** provou ser a solução de maior eficiência e estabilidade para o processamento de arquivos de texto de médio porte.
* O uso de **GPU** via OpenCL mostra-se uma tecnologia formidável, porém sua aplicação se restringe a cenários de *Big Data* (arquivos na escala de Gigabytes), onde o volume de processamento matemático massivo seja grande o suficiente para diluir a latência física imposta pelo barramento de comunicação do hardware.

## 7. Referências
* **JOCL.org.** *Java bindings for OpenCL*. Disponível em: <http://www.jocl.org/>.
* **JFreeChart.** *Free Java chart library*. Disponível em: <https://www.jfree.org/jfreechart/>.
* **ORACLE.** *The Java Tutorials: Concurrency*. Disponível em: <https://docs.oracle.com/javase/tutorial/essential/concurrency/>.

## 8. Anexos
O repositório completo com o histórico de desenvolvimento, os códigos-fonte da interface visual Swing, o exportador CSV e os algoritmos de busca concorrente em CPU/GPU podem ser acessados publicamente no link abaixo:

**Repositório Oficial:** <https://github.com/Alyra004/comparador-algoritmos>
