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
  int2 cell = extractCell(nodes, cell_Id);
  double4 plane = extractPlane(planes, cell_Id);
  if (cell.x == -1 && cell.y == -1) {
    if (plane.w == SOLID_MARKER) {
      return beginPoint;
    }
    return NULL;
  } else {
    int poligonType = 0;
    int types[2];
    double t = dot(beginPoint, plane.xyz);
  }
  return NULL;
}

__kernel void check_intersection(__global double* begin,
                                 __global double* end,
                                 __constant int* nodes,
                                 __constant double* planes,
                                 __private long cellNum,
                                 __global double3* results) {
  int index = get_global_id(0);
  double3 result;
  double3 beginPoint;
  beginPoint = (double3) (begin[0], begin[1], begin[2]);
  double3 endPoint;
  endPoint = (double3) (end[0], end[1], end[2]);
  results[index] = intersect(beginPoint, endPoint, nodes, planes, cellNum - 1);
}
