#ifndef __MY_SDL_H__
#define __MY_SDL_H__

#include <sys/time.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>


#define SDL_malloc	malloc
#define SDL_calloc	calloc
#define SDL_free	free


/** The SDL mutex structure, defined in SDL_mutex.c */
struct SDL_mutex;
typedef struct SDL_mutex SDL_mutex;

/** The SDL condition variable structure, defined in SDL_cond.c */
struct SDL_cond;
typedef struct SDL_cond SDL_cond;

void SDL_SetError (const char *fmt, ...);

SDL_mutex *SDL_CreateMutex (void);
void SDL_DestroyMutex(SDL_mutex *mutex);

#define SDL_LockMutex(m)	SDL_mutexP(m)
/** Lock the mutex
 *  @return 0, or -1 on error
 */
int SDL_mutexP(SDL_mutex *mutex);

#define SDL_UnlockMutex(m)	SDL_mutexV(m)
/** Unlock the mutex
 *  @return 0, or -1 on error
 *
 *  It is an error to unlock a mutex that has not been locked by
 *  the current thread, and doing so results in undefined behavior.
 */
int SDL_mutexV(SDL_mutex *mutex);


SDL_cond * SDL_CreateCond(void);
void SDL_DestroyCond(SDL_cond *cond);
int SDL_CondSignal(SDL_cond *cond);
int SDL_CondWait(SDL_cond *cond, SDL_mutex *mutex);



#endif
