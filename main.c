/*
* main.c
*
* Created: 21-10-2015 11:48:30
*  Author: IHA
*/


#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <stdio.h>
#include <string.h>

#include "include/board.h"
/* Scheduler include files. */
#include "FreeRTOS/Source/include/FreeRTOS.h"

#include "task.h"
#include "croutine.h"

#define startup_TASK_PRIORITY				( tskIDLE_PRIORITY )
#define just_a_task_TASK_PRIORITY			( tskIDLE_PRIORITY + 1 )


static const uint8_t _BT_RX_QUEUE_LENGTH = 30; 
static SemaphoreHandle_t  goal_line_semaphore = NULL;
static QueueHandle_t _xBT_received_chars_queue = NULL;


uint16_t accData[8]= {0,100,150,200,250,70,400,0};
uint8_t nextVal=0;
uint8_t bt_initialised = 0;
uint8_t charCount=0;
char sendValue[5] = {};
int tick;

void bt_status_call_back(uint8_t status) {
	if (status == DIALOG_OK_STOP) {
		bt_initialised = 1;
		} else if (status == DIALOG_ERROR_STOP) {
		// What to do??
	}
}

void bt_com_call_back(uint8_t byte) {
	char buf[40];
	
	if (bt_initialised) {
		switch (byte) {
			case 'a': {
				set_head_light(0);
				break;
			}
			
			case 'A': {
				set_head_light(1);
				break;
			}
			
			case 'b': {
				set_brake_light(0);
				break;
			}
			
			case 'B': {
				set_brake_light(1);
				break;
			}
			
			case 'c': {
				set_horn(0);
				break;
			}
			
			case 'C': {
				set_horn(1);
				break;
			}
			
			case 'd': {
				set_motor_speed(0);
				break;
			}
			
			case 'D': {
				set_motor_speed(65);
				break;
			}
			
			case 'e': {
				set_brake(0);
				break;
			}
			
			case 'E': {
				set_brake(100);
				break;
			}
			
			case 'F': {
				uint16_t raw_x = get_raw_x_accel();
				uint16_t raw_y = get_raw_y_accel();
				tick = xTaskGetTickCount();
				//uint16_t raw_z = get_raw_z_accel();
				uint16_t raw_rx = get_raw_x_rotation();
				uint16_t raw_ry = get_raw_y_rotation();
				uint16_t tacho = get_tacho_count();
				sprintf(buf, "x%4dy%4dz%4dr%4dq%4dt%4d", raw_x, raw_y, tick, raw_rx, raw_ry, tacho);
				bt_send_bytes((uint8_t *)buf, strlen(buf));
				break;
			}
			
			case 'f': {
				learn();
				break;
			}
			
			case 'G': {
				uint16_t raw_x = get_raw_x_rotation();
				uint16_t raw_y = get_raw_y_rotation();
				uint16_t raw_xa = get_raw_x_accel();
				sprintf(buf, "x%4d y%4d x%4d", raw_x, raw_y, raw_xa);
				bt_send_bytes((uint8_t *)buf, strlen(buf));
				break;
			}
			case 'N': {
				tick = xTaskGetTickCount();
				sprintf(buf, "%d", tick);
				bt_send_bytes((uint8_t *)buf, strlen(buf));
				break;
			}
			
			case 'P': {
				plannedTrack();
				break;
			}
			
			default:
				sendValue[charCount] = byte;
				charCount++;
				if(byte == 33) {
					accData[nextVal] = atoi(sendValue);
					nextVal++;
					charCount=0;
				}
				break;
		}
	}
}

void learn() {
	int i;
	char buf[20];
	
	for (i= 0; i<100; i++)
	{
			uint16_t raw_x = get_raw_x_accel();
			uint16_t raw_y = get_raw_y_accel();
			uint16_t raw_z = get_raw_z_accel();
			//uint16_t raw_rx = get_raw_x_rotation();
			//uint16_t raw_ry = get_raw_y_rotation();
			sprintf(buf, "%4d %4d %4d", raw_x, raw_y, raw_z);
			bt_send_bytes((uint8_t *)buf, strlen(buf));
			vTaskDelay( 100/ portTICK_PERIOD_MS);
	}
}

void plannedTrack() {
	uint16_t count = 0;
	//uint16_t prog[200]= {0,100,200,200};
	uint16_t tacho = get_tacho_count();
	//set_motor_speed(60);
	while (tacho < 800)
	{
		if (tacho >= accData[count])
		{
			if (accData[count+1] <= 100)
			{
				set_brake_light(0);
				set_motor_speed(accData[count+1]);
			} else if (accData[count+1] > 100) {
				set_brake(accData[count+1]-100);
				set_brake_light(1);
				set_motor_speed(0);
			} else {
				set_motor_speed(0);
			}
			nextVal= accData[count+1];
			count = count + 2;
		}
		tacho = tacho + get_tacho_count();
	}
	set_brake_light(0);
	set_motor_speed(0);
	count +1;
}


static void vjustATask( void *pvParameters ) {
	/* The parameters are not used. */
	( void ) pvParameters;

	/* Cycle for ever, one cycle each time the goal line is passed. */
	for( ;; )
	{
		// Wait for goal line is passed
		xSemaphoreTake(goal_line_semaphore, portMAX_DELAY);
	}
}

static void vstartupTask( void *pvParameters ) {
	/* The parameters are not used. */
	( void ) pvParameters;
	
	goal_line_semaphore = xSemaphoreCreateBinary();
	_xBT_received_chars_queue = xQueueCreate( _BT_RX_QUEUE_LENGTH, ( unsigned portBASE_TYPE ) sizeof( uint8_t ) );
	
	if( goal_line_semaphore == NULL ) {
		/* There was insufficient OpenRTOS heap available for the semaphore to
		be created successfully. */
		// What to do here ?????????????????????????????????
		} else {
		set_goal_line_semaphore(goal_line_semaphore);
	}
	
	// Initialize Bluetooth Module
	vTaskDelay( 1000/ portTICK_PERIOD_MS);
	set_bt_reset(0);  // Disable reset line of Blue tooth module
	vTaskDelay( 1000/ portTICK_PERIOD_MS);
	init_bt_module(bt_status_call_back, _xBT_received_chars_queue);
	
	xTaskCreate( vjustATask, "JustATask", configMINIMAL_STACK_SIZE, NULL, just_a_task_TASK_PRIORITY, NULL );
	uint8_t _byte;
	for( ;; ) {
		xQueueReceive( _xBT_received_chars_queue, &_byte, portMAX_DELAY );
		bt_com_call_back(_byte);
	}
}

int main(void)
{
	init_main_board();
	xTaskCreate( vstartupTask, "StartupTask", configMINIMAL_STACK_SIZE, NULL, startup_TASK_PRIORITY, NULL );
	vTaskStartScheduler();
}


// Called is TASK Stack overflows
void vApplicationStackOverflowHook( TaskHandle_t xTask, char *pcTaskName ) {
	//set_horn(1);
}
