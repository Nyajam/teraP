import java.util.*;

/*
    Prototipo de servicio interno.
    Autor: Ildefonso Macarro Pueyo
    Mail: fusm7id@gmail.com
    Fecha: 28-09-2021
*/

public class Serv
{
    //Variables de uso
    private LinkedList<String> slaves;
    private LinkedList<String> cabins;

    public Serv()
    {
        slaves=new LinkedList<>();
        cabins=new LinkedList<>();
    }

    /**
     * Arranca el servicio local
     * @return
     * 0 si al finalizar no ha habido problema
     */
    public int run()
    {
        int tmp;
        tmp=chargeValues();
        //Varificar donde se coloca el bucle
        //Abrir el socket
        //Recepcion de ordenes
        //Descodificacion de la orden
        //Ejecucion de la orden
        //Retorno
        return 0;
    }

    /**
     * Metodo que relaciona codigos de operacion con ejecuciones concretas.
     * @param code 
     * Tipo int, indice de operacion.
     * @param args
     * Tipo String, argumentos de la operacion.
     * @return
     * Salida de la operacion, 0 correcto.
     * 
     * Tabla:
     *  0: Parada de emergencia del cluster.
     *  1: Anadir nodo slave. IP
     *  2: Borrar nodo slave. IP
     *  3: Parar nodo slave (las vm). IP
     *  4: Actualizar pools de nodo slave. IP
     *  5: Anadir nodo cabin. IP
     *  6: Borrar nodo cabin. IP
     *  7: Crear pool.
     *  8: Borrar pool.
     *  9: Anadir disco a pool.
     *  10: Borrar disco de una pool.
     *  11: 
     */
    private int index(int code, String... args)
    {
        switch(code)
        {
            case 0:
            {
                return 0;
            }
            default:
            {
                return 0;
            }
        }
    }

    /**
     * Carga los valores de la base de datos.
     * @return
     * 0 si la operacion de conexion con la db y la carga ha salido satisfactorio
     */
    private int chargeValues()
    {
        for(int i=0;i<10;i++)
        {
            slaves.add("192.168.0.10"+i);
            cabins.add("192.168.0.11"+i);
        }
        return 0;
    }

    /**
     * Imprime por pantalla el estado del objeto
     */
    public void debug()
    {
        /*
        Lista de slaves:
            ·Size: 1
            ·Elementos: []
        Lista de cabins:
            ·Size: 1
            ·Elementos: []
        */
        System.out.println("");
    }

    public static void main(String[] args)
    {
        int exit;
        System.out.println("Inicializando servicio de prueba...");
        exit=new Serv().run();
        System.out.println("Servicio de prueba finalizado");
        System.out.println("Finalizado con: "+exit);
    }
}