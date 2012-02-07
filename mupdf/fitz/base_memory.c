#include "fitz.h"

static void *
do_scavenging_malloc(fz_context *ctx, unsigned int size)
{
	void *p;
	int phase = 0;

	fz_lock(ctx);
	do {
		p = ctx->alloc->malloc(ctx->alloc->user, size);
		if (p != NULL)
		{
			fz_unlock(ctx);
			return p;
		}
	} while (fz_store_scavenge(ctx, size, &phase));
	fz_unlock(ctx);

	return NULL;
}

static void *
do_scavenging_realloc(fz_context *ctx, void *p, unsigned int size)
{
	void *q;
	int phase = 0;

	fz_lock(ctx);
	do {
		q = ctx->alloc->realloc(ctx->alloc->user, p, size);
		if (q != NULL)
		{
			fz_unlock(ctx);
			return q;
		}
	} while (fz_store_scavenge(ctx, size, &phase));
	fz_unlock(ctx);

	return NULL;
}

void *
fz_malloc(fz_context *ctx, unsigned int size)
{
	void *p;

	if (size == 0)
		return NULL;

	p = do_scavenging_malloc(ctx, size);
	if (!p)
		fz_throw(ctx, "malloc of %d bytes failed", size);
	return p;
}

void *
fz_malloc_no_throw(fz_context *ctx, unsigned int size)
{
	return do_scavenging_malloc(ctx, size);
}

void *
fz_malloc_array(fz_context *ctx, unsigned int count, unsigned int size)
{
	void *p;

	if (count == 0 || size == 0)
		return 0;

	if (count > UINT_MAX / size)
		fz_throw(ctx, "malloc of array (%d x %d bytes) failed (integer overflow)", count, size);

	p = do_scavenging_malloc(ctx, count * size);
	if (!p)
		fz_throw(ctx, "malloc of array (%d x %d bytes) failed", count, size);
	return p;
}

void *
fz_malloc_array_no_throw(fz_context *ctx, unsigned int count, unsigned int size)
{
	if (count == 0 || size == 0)
		return 0;

	if (count > UINT_MAX / size)
	{
		fprintf(stderr, "error: malloc of array (%d x %d bytes) failed (integer overflow)", count, size);
		return NULL;
	}

	return do_scavenging_malloc(ctx, count * size);
}

void *
fz_calloc(fz_context *ctx, unsigned int count, unsigned int size)
{
	void *p;

	if (count == 0 || size == 0)
		return 0;

	if (count > UINT_MAX / size)
	{
		fz_throw(ctx, "calloc (%d x %d bytes) failed (integer overflow)", count, size);
	}

	p = do_scavenging_malloc(ctx, count * size);
	if (!p)
	{
		fz_throw(ctx, "calloc (%d x %d bytes) failed", count, size);
	}
	memset(p, 0, count*size);
	return p;
}

void *
fz_calloc_no_throw(fz_context *ctx, unsigned int count, unsigned int size)
{
	void *p;

	if (count == 0 || size == 0)
		return 0;

	if (count > UINT_MAX / size)
	{
		fprintf(stderr, "error: calloc (%d x %d bytes) failed (integer overflow)\n", count, size);
		return NULL;
	}

	p = do_scavenging_malloc(ctx, count * size);
	if (p)
	{
		memset(p, 0, count*size);
	}
	return p;
}

void *
fz_resize_array(fz_context *ctx, void *p, unsigned int count, unsigned int size)
{
	void *np;

	if (count == 0 || size == 0)
	{
		fz_free(ctx, p);
		return 0;
	}

	if (count > UINT_MAX / size)
		fz_throw(ctx, "resize array (%d x %d bytes) failed (integer overflow)", count, size);

	np = do_scavenging_realloc(ctx, p, count * size);
	if (!np)
		fz_throw(ctx, "resize array (%d x %d bytes) failed", count, size);
	return np;
}

void *
fz_resize_array_no_throw(fz_context *ctx, void *p, unsigned int count, unsigned int size)
{
	if (count == 0 || size == 0)
	{
		fz_free(ctx, p);
		return 0;
	}

	if (count > UINT_MAX / size)
	{
		fprintf(stderr, "error: resize array (%d x %d bytes) failed (integer overflow)\n", count, size);
		return NULL;
	}

	return do_scavenging_realloc(ctx, p, count * size);
}

void
fz_free(fz_context *ctx, void *p)
{
	fz_lock(ctx);
	ctx->alloc->free(ctx->alloc->user, p);
	fz_unlock(ctx);
}

char *
fz_strdup(fz_context *ctx, char *s)
{
	int len = strlen(s) + 1;
	char *ns = fz_malloc(ctx, len);
	memcpy(ns, s, len);
	return ns;
}

char *
fz_strdup_no_throw(fz_context *ctx, char *s)
{
	int len = strlen(s) + 1;
	char *ns = fz_malloc_no_throw(ctx, len);
	if (ns)
		memcpy(ns, s, len);
	return ns;
}

static void *
fz_malloc_default(void *opaque, unsigned int size)
{
	return malloc(size);
}

static void *
fz_realloc_default(void *opaque, void *old, unsigned int size)
{
	return realloc(old, size);
}

static void
fz_free_default(void *opaque, void *ptr)
{
	free(ptr);
}

fz_alloc_context fz_alloc_default =
{
	NULL,
	fz_malloc_default,
	fz_realloc_default,
	fz_free_default
};
