# NumberTokenGenerator
Generates a token (number) distributively

`Generator.java` class acts as both master and slave program

#### RUN COMMANDS:

  Master Node
   - Program Arguments (port_number):
     - `9090`

  Slave Nodes
   - Program Arguments (port_number master_get_api):
     - `9001 http://localhost:9090/getrange`


