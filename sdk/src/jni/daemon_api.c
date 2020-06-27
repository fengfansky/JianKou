#include <stdio.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/inotify.h>

#include "log.h"
#include "constant.h"
#include "com_arrownock_internal_push_NativeAPIs.h"


/**
 *  get the process pid by process name
 */
int find_pid_by_name(char *pid_name, int *pid_list){
    DIR *dir;
	struct dirent *next;
	int i = 0;
	pid_list[0] = 0;
	dir = opendir("/proc");
	if (!dir){
		return 0;
	}
	while ((next = readdir(dir)) != NULL){
		FILE *status;
		char proc_file_name[BUFFER_SIZE];
		char buffer[BUFFER_SIZE];
		char process_name[BUFFER_SIZE];

		if (strcmp(next->d_name, "..") == 0){
			continue;
		}
		if (!isdigit(*next->d_name)){
			continue;
		}
		sprintf(proc_file_name, "/proc/%s/cmdline", next->d_name);
		if (!(status = fopen(proc_file_name, "r"))){
			continue;
		}
		if (fgets(buffer, BUFFER_SIZE - 1, status) == NULL){
			fclose(status);
			continue;
		}
		fclose(status);
		sscanf(buffer, "%[^-]", process_name);
		if (strcmp(process_name, pid_name) == 0){
			pid_list[i ++] = atoi(next->d_name);
		}
	}
	if (pid_list){
    	pid_list[i] = 0;
    }
    closedir(dir);
    return i;
}

/**
 *  kill all process by name
 */
void kill_zombie_process(char* zombie_name){
    int pid_list[200];
    int total_num = find_pid_by_name(zombie_name, pid_list);
    LOGD("Process is %s, %d, stopping...", zombie_name, total_num);
    int i;
    for (i = 0; i < total_num; i ++)    {
        int retval = 0;
        int daemon_pid = pid_list[i];
        if (daemon_pid > 1 && daemon_pid != getpid() && daemon_pid != getppid()){
            retval = kill(daemon_pid, SIGTERM);
            if (!retval){
                LOGD("Stopped: %d", daemon_pid);
            }else{
                LOGE("Stop failed: %d", daemon_pid);
            }
        }
    }
}

JNIEXPORT void JNICALL Java_com_arrownock_internal_push_NativeAPIs_runDaemon(JNIEnv *env, jobject jobj, jstring pkgName, jstring svcName, jstring daemonPath){
	if(pkgName == NULL || svcName == NULL || daemonPath == NULL){
		LOGE("Native API call failed: parameters are empty!");
		return ;
	}

	char *pkg_name = (char*)(*env)->GetStringUTFChars(env, pkgName, 0);
	char *svc_name = (char*)(*env)->GetStringUTFChars(env, svcName, 0);
	char *daemon_path = (char*)(*env)->GetStringUTFChars(env, daemonPath, 0);

	// 20161219 update daemon name
	//kill_zombie_process(NATIVE_DAEMON_NAME);
	char daemon_name[strlen(pkg_name) + 7];
	strcpy(daemon_name, pkg_name);
	strcat(daemon_name, "_daemon");
	kill_zombie_process(daemon_name);
	LOGD("Daemon: %s", daemon_name);
	// end

	int pipe_fd1[2];//order to watch child
	int pipe_fd2[2];//order to watch parent

	pid_t pid;
	char r_buf[100];
	int r_num;
	memset(r_buf, 0, sizeof(r_buf));
	if(pipe(pipe_fd1)<0){
		LOGE("pipe1 create error");
		return ;
	}
	if(pipe(pipe_fd2)<0){
		LOGE("pipe2 create error");
		return ;
	}

	char str_p1r[10];
	char str_p1w[10];
	char str_p2r[10];
	char str_p2w[10];

	sprintf(str_p1r,"%d",pipe_fd1[0]);
	sprintf(str_p1w,"%d",pipe_fd1[1]);
	sprintf(str_p2r,"%d",pipe_fd2[0]);
	sprintf(str_p2w,"%d",pipe_fd2[1]);


	if((pid=fork())==0){
		execlp(daemon_path,
				// 20161219 update daemon name
				daemon_name,
				//NATIVE_DAEMON_NAME(pkg_name),
				// end
				PARAM_PKG_NAME, pkg_name,
				PARAM_SVC_NAME, svc_name,
				PARAM_PIPE_1_READ, str_p1r,
				PARAM_PIPE_1_WRITE, str_p1w,
				PARAM_PIPE_2_READ, str_p2r,
				PARAM_PIPE_2_WRITE, str_p2w,
				(char *) NULL);
	}else if(pid>0){
		close(pipe_fd1[1]);
		close(pipe_fd2[0]);
		//wait for child
		r_num=read(pipe_fd1[0], r_buf, 100);
		LOGE("Daemon has ended...");
		java_callback(env, jobj, DAEMON_CALLBACK_NAME);
	}
}

notify_and_waitfor(char *observer_self_path, char *observer_daemon_path){
	int observer_self_descriptor = open(observer_self_path, O_RDONLY);
	if (observer_self_descriptor == -1){
		observer_self_descriptor = open(observer_self_path, O_CREAT, S_IRUSR | S_IWUSR);
	}
	int observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
	while (observer_daemon_descriptor == -1){
		usleep(1000);
		observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
	}
	remove(observer_daemon_path);
	LOGD("Observer is ready...");
}


/**
 *  Lock the file, this is block method.
 */
int lock_file(char* lock_file_path){
    LOGD("Locking file: %s", lock_file_path);
    int lockFileDescriptor = open(lock_file_path, O_RDONLY);
    if (lockFileDescriptor == -1){
        lockFileDescriptor = open(lock_file_path, O_CREAT, S_IRUSR);
    }
    int lockRet = flock(lockFileDescriptor, LOCK_EX);
    if (lockRet == -1){
        LOGE("Lock file failed: %s", lock_file_path);
        return 0;
    }else{
        LOGD("Lock file success: %s", lock_file_path);
        return 1;
    }
}


JNIEXPORT void JNICALL Java_com_arrownock_internal_push_NativeAPIs_startDaemon(JNIEnv *env, jobject jobj, jstring indicatorSelfPath, jstring indicatorDaemonPath, jstring observerSelfPath, jstring observerDaemonPath){
	if(indicatorSelfPath == NULL || indicatorDaemonPath == NULL || observerSelfPath == NULL || observerDaemonPath == NULL){
		LOGE("Native API call error: parameters are empty!");
		return ;
	}
	char* indicator_self_path = (char*)(*env)->GetStringUTFChars(env, indicatorSelfPath, 0);
	char* indicator_daemon_path = (char*)(*env)->GetStringUTFChars(env, indicatorDaemonPath, 0);
	char* observer_self_path = (char*)(*env)->GetStringUTFChars(env, observerSelfPath, 0);
	char* observer_daemon_path = (char*)(*env)->GetStringUTFChars(env, observerDaemonPath, 0);

	int lock_status = 0;
	int try_time = 0;
	while(try_time < 3 && !(lock_status = lock_file(indicator_self_path))){
		try_time++;
		LOGD("Lock myself failed and try again as %d times", try_time);
		usleep(10000);
	}
	if(!lock_status){
		LOGE("Lock myself failed and exit");
		return ;
	}

//	notify_daemon_observer(observer_daemon_path);
//	waitfor_self_observer(observer_self_path);
	notify_and_waitfor(observer_self_path, observer_daemon_path);
	lock_status = lock_file(indicator_daemon_path);
	if(lock_status){
		LOGW("Push service daemon ending...");
		remove(observer_self_path);// it`s important ! to prevent from deadlock
		java_callback(env, jobj, DAEMON_CALLBACK_NAME);
	}

}
