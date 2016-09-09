package joetater.common;

public class RegionCriteriaFactory
{
	public static RegionCriteria inRadius(final int originX, final int originZ, final int radius)
	{
		return new RegionCriteria()
		{
			@Override
			public int getMinX()
			{
				return originX - radius;
			}
	
			@Override
			public int getMaxX()
			{
				return originX + radius;
			}
	
			@Override
			public int getMinZ()
			{
				return originZ - radius;
			}
	
			@Override
			public int getMaxZ()
			{
				return originZ + radius;
			}
		};
	}

	public static RegionCriteria inBox(final int xMin, final int xMax, final int zMin, final int zMax)
	{
		return new RegionCriteria()
		{
			@Override
			public int getMinX()
			{
				return xMin;
			}
	
			@Override
			public int getMaxX()
			{
				return xMax;
			}
	
			@Override
			public int getMinZ()
			{
				return zMin;
			}
	
			@Override
			public int getMaxZ()
			{
				return zMax;
			}
		};
	}
}
