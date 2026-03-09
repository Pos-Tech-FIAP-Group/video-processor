import { RouteReuseStrategy, ActivatedRouteSnapshot, DetachedRouteHandle } from '@angular/router';

/**
 * Reutiliza o componente da lista de vídeos ao alternar entre /videos e /videos/:id,
 * evitando o "piscar" ao abrir e fechar o modal de detalhe.
 */
function isVideosListRoute(snapshot: ActivatedRouteSnapshot): boolean {
  const path = snapshot.routeConfig?.path;
  const parentPath = snapshot.parent?.routeConfig?.path;
  return parentPath === 'videos' && (path === '' || path === ':id');
}

export class VideosListReuseStrategy implements RouteReuseStrategy {
  shouldReuseRoute(
    future: ActivatedRouteSnapshot,
    curr: ActivatedRouteSnapshot,
  ): boolean {
    if (future.routeConfig === curr.routeConfig) return true;
    return isVideosListRoute(future) && isVideosListRoute(curr);
  }

  shouldAttach(_route: ActivatedRouteSnapshot): boolean {
    return false;
  }

  shouldDetach(_route: ActivatedRouteSnapshot): boolean {
    return false;
  }

  retrieve(_route: ActivatedRouteSnapshot): DetachedRouteHandle | null {
    return null;
  }

  store(_route: ActivatedRouteSnapshot, _handle: DetachedRouteHandle): void {}
}
