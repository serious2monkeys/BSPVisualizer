#pragma OPENCL EXTENSION cl_khr_fp64 : enable
#define COMPLANAR 0
#define FRONT 1
#define BACK 2
#define SPANNING 3
#define EPSILON 1e-6
#define SOLID_MARKER -1


double4 extractPlane(__constant double* planes,
                     __private long cell) {
  int index = (int) cell << 2;
  return (double4) (planes[index], planes[index + 1], planes[index + 2], planes[index + 3]);
}

int2 extractCell(__constant int* nodes,
                __private long cell) {
    int index = (int) cell << 1;
    return (int2) (nodes[index], nodes[index + 1]);
}

double3 findIntersection(__private double3 beginPoint,
                         __private double3 endPoint,
                         __private double4 plane) {
    double3 dirVector = endPoint - beginPoint;
    double t = (dot(beginPoint, plane.xyz) - plane.w) / dot(dirVector, plane.xyz) * (-1);
    double3 transformed = dirVector * t;
    return beginPoint + transformed;
}

double3 intersect(__private double3 beginPoint,
                  __private double3 endPoint,
                  __constant int* nodes,
                  __constant double* planes,
                  __private long cell_Id) {
  bool finished = false;
  double3 result;
  result = NULL;
  long workCell;
  workCell = cell_Id;
  double3 workBeginPoint, workEndPoint;
  workBeginPoint = beginPoint;
  workEndPoint = endPoint;

  bool hasAlternative = false;
  double3 alternativeBegin, alternativeEnd;
  long alternativeWorkCell;

  do {
    int2 cell = extractCell(nodes, workCell);
    double4 plane = extractPlane(planes, workCell);
    if (cell.x == -1 && cell.y == -1) {
      if (plane.w == SOLID_MARKER) {
        finished = true;
        return workBeginPoint;
      } else {
        if (hasAlternative) {
          hasAlternative = false;
          workBeginPoint = alternativeBegin;
          workEndPoint = alternativeEnd;
          workCell = alternativeWorkCell;
          continue;
        }
        return (double3)(NULL);
      }
    } else {
      int polygonType = 0;
      int types[2];
      double t = dot(workBeginPoint, plane.xyz) - plane.w;
      types[0] = t < - EPSILON ? BACK : t > EPSILON ? FRONT : COMPLANAR;
      polygonType |= types[0];
      t = dot(workEndPoint, plane.xyz) - plane.w;
      types[1] = t < - EPSILON ? BACK : t > EPSILON ? FRONT : COMPLANAR;
      polygonType |= types[1];
      switch(polygonType) {
        case COMPLANAR:
          finished = true;
          return (double3)(NULL);
          break;
        case FRONT:
          workCell= cell[0];
          continue;
          break;
        case BACK:
          workCell = cell[1];
          continue;
          break;
        case SPANNING: {
          double3 intersection = findIntersection(workBeginPoint, workEndPoint, plane);
          double3 frontPart[2], backPart[2];
          frontPart[0] = types[0] == FRONT ? workBeginPoint : intersection;
          frontPart[1] = types[0] == FRONT ? intersection : workEndPoint;
          backPart[0] = types[0] == BACK ? workBeginPoint : intersection;
          backPart[1] = types[0] == BACK ? intersection : workEndPoint;
          long nearest = planes[(cell[0] << 2) + 3] < planes[(cell[1] << 2) + 3] ? cell[0] : cell[1];
          hasAlternative = true;
          if (nearest == cell[0]) {
            workBeginPoint = frontPart[0];
            workEndPoint = frontPart[1];
            workCell = cell[0];

            alternativeBegin = backPart[0];
            alternativeEnd = backPart[1];
            alternativeWorkCell = cell[1];
          } else {
            workBeginPoint = backPart[0];
            workEndPoint = backPart[1];
            workCell = cell[1];

            alternativeBegin = frontPart[0];
            alternativeEnd = frontPart[1];
            alternativeWorkCell = cell[0];
          }
          continue;
          break;
        }
      }
    }
    return (double3)(NULL);
  } while (!finished);
  return result;
}

__kernel void check_intersection(__global double* begin,
                                 __global double* end,
                                 __private long raysNum,
                                 __constant int* nodes,
                                 __constant double* planes,
                                 __private long cellNum,
                                 __global double* results) {
  int index = get_global_id(0);//get_global_id(0) * get_local_id(0);
  if (index < raysNum) {
    double3 result;
    double3 beginPoint;
    beginPoint = (double3) (begin[index*3], begin[index*3 + 1], begin[index*3 + 2]);
    double3 endPoint;
    endPoint = (double3) (end[index*3], end[index*3 + 1], end[index*3 + 2]);
    double3 point;
    point = intersect(beginPoint, endPoint, nodes, planes, cellNum - 1);
    if (point.x != NULL) {
      vstore3(point, 0, results + index * 3);
    }
  } else {
    return;
  }
}
