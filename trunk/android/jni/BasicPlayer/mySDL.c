#include "mySDL.h"
#include "BasicPlayerJni.h"

struct SDL_mutex {
	pthread_mutex_t id;
};

struct SDL_cond
{
	pthread_cond_t cond;
};


char buf[1024];

void SDL_SetError (const char *fmt, ...)
{
	va_list marker;
	va_start(marker, fmt);
	vsprintf(buf,fmt, marker);
	va_end(marker);
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, buf);	
}


SDL_mutex *SDL_CreateMutex (void)
{
	SDL_mutex *mutex;
	pthread_mutexattr_t attr;

	/* Allocate the structure */
	mutex = (SDL_mutex *)SDL_calloc(1, sizeof(*mutex));
	if ( mutex ) {
		pthread_mutexattr_init(&attr);
		if ( pthread_mutex_init(&mutex->id, &attr) != 0 ) {
			SDL_SetError("pthread_mutex_init() failed");
			SDL_free(mutex);
			mutex = NULL;
		}
	} else {
		SDL_SetError("Out of memory");
	}
	return(mutex);
}

void SDL_DestroyMutex(SDL_mutex *mutex)
{
	if ( mutex ) {
		pthread_mutex_destroy(&mutex->id);
		SDL_free(mutex);
	}
}

/* Create a condition variable */
SDL_cond * SDL_CreateCond(void)
{
	SDL_cond *cond;

	cond = (SDL_cond *) SDL_malloc(sizeof(SDL_cond));
	if ( cond ) {
		if ( pthread_cond_init(&cond->cond, NULL) < 0 ) {
			SDL_SetError("pthread_cond_init() failed");
			SDL_free(cond);
			cond = NULL;
		}
	}
	return(cond);
}

/* Destroy a condition variable */
void SDL_DestroyCond(SDL_cond *cond)
{
	if ( cond ) {
		pthread_cond_destroy(&cond->cond);
		SDL_free(cond);
	}
}

/* Restart one of the threads that are waiting on the condition variable */
int SDL_CondSignal(SDL_cond *cond)
{
	int retval;

	if ( ! cond ) {
		SDL_SetError("Passed a NULL condition variable");
		return -1;
	}

	retval = 0;
	if ( pthread_cond_signal(&cond->cond) != 0 ) {
		SDL_SetError("pthread_cond_signal() failed");
		retval = -1;
	}
	return retval;
}


/* Lock the mutex */
int SDL_mutexP(SDL_mutex *mutex)
{
	int retval;
	if ( mutex == NULL ) {
		SDL_SetError("Passed a NULL mutex");
		return -1;
	}

	retval = 0;
	if ( pthread_mutex_lock(&mutex->id) < 0 ) {
		SDL_SetError("pthread_mutex_lock() failed");
		retval = -1;
	}
	return retval;
}

int SDL_mutexV(SDL_mutex *mutex)
{
	int retval;

	if ( mutex == NULL ) {
		SDL_SetError("Passed a NULL mutex");
		return -1;
	}

	retval = 0;
	if ( pthread_mutex_unlock(&mutex->id) < 0 ) {
		SDL_SetError("pthread_mutex_unlock() failed");
		retval = -1;
	}

	return retval;
}


int SDL_CondWait(SDL_cond *cond, SDL_mutex *mutex)
{
	int retval;

	if ( ! cond ) {
		SDL_SetError("Passed a NULL condition variable");
		return -1;
	}

	retval = 0;
	if ( pthread_cond_wait(&cond->cond, &mutex->id) != 0 ) {
		SDL_SetError("pthread_cond_wait() failed");
		retval = -1;
	}
	return retval;
}
